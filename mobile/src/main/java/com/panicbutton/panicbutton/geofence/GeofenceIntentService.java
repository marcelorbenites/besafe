package com.panicbutton.panicbutton.geofence;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.useaurea.aurea.AureaBusinessLocation;
import com.useaurea.aurea.AureaException;
import com.useaurea.aurea.LogLevel;
import com.useaurea.aurea.internal.AureaApplicationResolver;
import com.useaurea.aurea.internal.data.CurrentStatusProvider;
import com.useaurea.aurea.internal.data.preferences.SharedPreferencesProviderFactory;
import com.useaurea.aurea.internal.log.Log;
import com.useaurea.aurea.internal.log.LogFactory;
import com.useaurea.aurea.internal.networking.AureaNetworkCallback;
import com.useaurea.aurea.internal.networking.AureaNetworkContract;
import com.useaurea.aurea.internal.networking.ContentHelper;
import com.useaurea.aurea.internal.networking.NetworkBus;
import com.useaurea.aurea.internal.networking.NetworkIntentService;

import java.util.List;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class GeofenceIntentService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AureaNetworkCallback {

    public static final int GEOFENCE_UPDATE = 0;
    public static final int GEOFENCE_STOP = 1;

    public static final String ACTION_GEOFENCE_INITIALIZE = "com.useaurea.aurea.action.GEOFENCE_INITIALIZE";
    public static final String ACTION_GEOFENCE_STOP = "com.useaurea.aurea.action.GEOFENCE_STOP";
    public static final String ACTION_GEOFENCE_UPDATE = "com.useaurea.aurea.action.GEOFENCE_UPDATE";

    private volatile Looper serviceLooper;
    private volatile ServiceHandler serviceHandler;

    private Log log;
    private AureaGoogleApiClient aureaGoogleApiClient;
    private LocationRequest locationRequest;

    private CurrentStatusProvider geofenceStatusProvider;
    private AureaBusinessLocationProvider businessLocationProvider;
    private NetworkBus networkBus;
    private Intent intent;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case GEOFENCE_UPDATE:
                    List<AureaBusinessLocation> newGeoFences = (List) msg.obj;
                    addGeofences(newGeoFences);
                    List<AureaBusinessLocation> oldGeoFences = businessLocationProvider.getAll();
                    oldGeoFences.removeAll(newGeoFences);
                    removeGeofences(oldGeoFences);
                    break;
                case GEOFENCE_STOP:
                    removeGeofences(businessLocationProvider.getAll());
                    break;
                default:
                    break;
            }
            stopSelf();
        }

        private void addGeofences(List<AureaBusinessLocation> geofences) {
            if (geofences.size() > 0) {
                persistGeofences(geofences);
                startMonitoringGeofences(geofences);
            }
        }

        private void removeGeofences(List<AureaBusinessLocation> geofences) {
            if (geofences.size() > 0) {
                deleteGeofences(geofences);
                stopMonitoringGeofences(geofences);
            }
        }
    }

    @SuppressWarnings("unchecked") @Override public void onCreate() {

        final HandlerThread thread = new HandlerThread("IntentService[com.useaurea.aurea.internal.geofence.GeofenceService]");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        final SharedPreferencesProviderFactory providerFactory = new SharedPreferencesProviderFactory(this);
        geofenceStatusProvider = providerFactory.getGeofenceStatusProvider();

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setFastestInterval(30 * 1000);
        locationRequest.setInterval(300 * 1000);
        locationRequest.setSmallestDisplacement(1000);

        final LogFactory logFactory = new LogFactory(providerFactory.getConfigurationProvider());
        log = logFactory.newLog("UseAureaGeofence");

        networkBus = NetworkBus.getInstance(this, logFactory.newLog("UseAureaNetwork"));

        final ContentHelper contentHelper = new ContentHelper(new AureaApplicationResolver(getPackageName()));
        businessLocationProvider = new AureaBusinessLocationProvider(getContentResolver(), contentHelper, logFactory.newLog("UseAureaProvider"));

        try {
            aureaGoogleApiClient = new AureaGoogleApiClient.Builder(this, log)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        } catch (ClassNotFoundException e) {
            log.internalLog(String.format("Aborting Geofence service. %s", e.getMessage()), LogLevel.ERROR);
            e.printStackTrace();
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        if (aureaGoogleApiClient != null) {
            if (aureaGoogleApiClient.isConnected()) {
                startAction(intent);
            } else if (!aureaGoogleApiClient.isConnecting()) {
                log.internalLog("Start Google API client connection.", LogLevel.DEBUG);
                aureaGoogleApiClient.connect();
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onConnected(Bundle bundle) {
        log.internalLog("Connected to Google API client successfully.", LogLevel.DEBUG);
        startAction(intent);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        log.internalLog("Google API client temporarily disconnected.", LogLevel.WARN);
        stopSelf();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        log.internalLog(String.format("Connection to Google API client failed with code %d.", connectionResult.getErrorCode()), LogLevel.ERROR);
        stopSelf();
    }

    private void startAction(Intent intent) {
        if (intent != null) {
            log.internalLog("Received Action.", LogLevel.DEBUG);
            if (ACTION_GEOFENCE_INITIALIZE.equals(intent.getAction())) {
                log.internalLog("Start geofence initilization.", LogLevel.DEBUG);
                requestLocationUpdates(locationRequest);
            } else if (ACTION_GEOFENCE_UPDATE.equals(intent.getAction())) {
                Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
                Location mockLocation = intent.getParcelableExtra(FusedLocationProviderApi.KEY_MOCK_LOCATION);

                if (mockLocation != null) {
                    log.internalLog(String.format("Mock Location changed - %s", mockLocation.toString()), LogLevel.DEBUG);
                }

                if (location != null) {
                    log.internalLog(String.format("Location changed - %s", location.toString()), LogLevel.DEBUG);
                } else {
                    location = mockLocation;
                }

                if (location != null) {
                    loadNewGeofences(location);
                    log.internalLog("Start geofence update.", LogLevel.DEBUG);
                }
            } else if (ACTION_GEOFENCE_STOP.equals(intent.getAction())) {
                log.internalLog("Start geofence stop.", LogLevel.DEBUG);
                removeLocationUpdatesRequest();
            }
        }
    }

    private void removeLocationUpdatesRequest() {
        LocationServices.FusedLocationApi.removeLocationUpdates(aureaGoogleApiClient.getGoogleApiClient(), getGeofencePendingIntent(GeofenceIntentService.class, GeofenceIntentService.ACTION_GEOFENCE_UPDATE)).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    geofenceStatusProvider.set(CurrentStatusProvider.Status.STOPPED);
                    log.internalLog("Unregistered for location updates.", LogLevel.DEBUG);
                    startActionInBackground(GEOFENCE_STOP);
                } else {
                    log.internalLog(String.format("Failed to remove location updates with status %s.", status.toString()), LogLevel.ERROR);
                }
            }
        });
    }

    private void requestLocationUpdates(final LocationRequest locationRequest) {
        LocationServices.FusedLocationApi.requestLocationUpdates(aureaGoogleApiClient.getGoogleApiClient(), locationRequest, getGeofencePendingIntent(GeofenceIntentService.class, GeofenceIntentService.ACTION_GEOFENCE_UPDATE)).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    geofenceStatusProvider.set(CurrentStatusProvider.Status.RUNNING);
                    log.internalLog(String.format("Registered for location updates with request: %s.", locationRequest.toString()), LogLevel.DEBUG);
                } else {
                    log.internalLog(String.format("Failed to register for location updates with status %s.", status.toString()), LogLevel.ERROR);
                }
            }
        });
    }

    private void loadNewGeofences(Location location) {
        if (location != null) {
            log.internalLog("Loading geofences...", LogLevel.DEBUG);
            Uri uri = getGeofencesRequest(location, 10);
            networkBus.postRequest(uri, NetworkIntentService.ACTION_GET, this, false);
        } else {
            log.internalLog("Invalid location. Geofences won't be loaded.", LogLevel.ERROR);
            stopSelf();
        }
    }

    private Uri getGeofencesRequest(Location location, int limit) {
        return AureaNetworkContract.Geofences.CONTENT_URI.buildUpon()
                .appendQueryParameter(AureaNetworkContract.GeofencesColumns.LAT, String.valueOf(location.getLatitude()))
                .appendQueryParameter(AureaNetworkContract.GeofencesColumns.LONG, String.valueOf(location.getLongitude()))
                .appendQueryParameter(AureaNetworkContract.GeofencesColumns.LIMIT, String.valueOf(limit)).build();
    }

    private void startMonitoringGeofences(List<AureaBusinessLocation> geofences) {
        final int size = geofences.size();
        if (size > 0) {
            Status status = LocationServices.GeofencingApi.addGeofences(aureaGoogleApiClient.getGoogleApiClient(), GeofenceAdapter.toGeofences(geofences), getGeofencePendingIntent(GeofenceEventIntentService.class)).await();
            if (status.isSuccess()) {
                log.internalLog(String.format("Started monitoring %d geofences.", size), LogLevel.DEBUG);
            } else {
                log.internalLog(String.format("Failed to monitor geofences with status %s.", status.toString()), LogLevel.ERROR);
            }
        } else {
            log.internalLog("No geofences to monitor.", LogLevel.DEBUG);
        }
    }

    private PendingIntent getGeofencePendingIntent(Class serviceClass) {
        return PendingIntent.getService(this, 0, new Intent(this, serviceClass), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getGeofencePendingIntent(Class serviceClass, String action) {

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            // Workaround regarding pending intent not delivered bug when app is reinstalled.
            PendingIntent.getService(this, 0, new Intent(this, serviceClass).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT).cancel();
        }

        return PendingIntent.getService(this, 0, new Intent(this, serviceClass).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void persistGeofences(List<AureaBusinessLocation> geofences) {
        int size = geofences.size();
        AureaBusinessLocation geofence;
        for (int i = 0; i < size; i++) {
            geofence = geofences.get(i);
            geofence.setId(businessLocationProvider.insert(geofence));
        }
        log.internalLog(String.format("Added %d geofences.", size), LogLevel.DEBUG);
    }

    private void stopMonitoringGeofences(List<AureaBusinessLocation> geofences) {
        final int size = geofences.size();
        if (size > 0) {
            Status status = LocationServices.GeofencingApi.removeGeofences(aureaGoogleApiClient.getGoogleApiClient(), GeofenceAdapter.toGeofenceIdList(geofences)).await();
            if (status.isSuccess()) {
                log.internalLog(String.format("Stopped monitoring %d geofences.", size), LogLevel.DEBUG);
            } else {
                log.internalLog(String.format("Failed stop monitoring geofences with status %s.", status.toString()), LogLevel.ERROR);
            }
        } else {
            log.internalLog("No geofences being monitored.", LogLevel.DEBUG);
        }
    }

    private void deleteGeofences(List<AureaBusinessLocation> geofences) {
        int size = geofences.size();
        for (int i = 0; i < size; i++) {
            businessLocationProvider.delete(geofences.get(i).getId());
        }
        log.internalLog(String.format("Removed %d geofences.", size), LogLevel.DEBUG);
    }

    @Override
    public void success(Object result, Uri resource) {
        if (ContentHelper.matchUris(AureaNetworkContract.Geofences.CONTENT_URI, resource)) {
            log.internalLog("Geofences loaded.", LogLevel.DEBUG);
            startActionInBackground(GEOFENCE_UPDATE, result);
        }
    }

    @Override
    public void error(AureaException error, Uri resource) {
        log.internalLog(String.format("Error loading geofences - %s.", error.getMessage()), LogLevel.ERROR);
        stopSelf();
    }

    private void startActionInBackground(int action) {
        startActionInBackground(action, null);
    }

    private void startActionInBackground(int action, Object object) {
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = action;
        msg.obj = object;
        serviceHandler.sendMessage(msg);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        log.internalLog("Geofence service finished.", LogLevel.DEBUG);
        if (aureaGoogleApiClient != null) {
            aureaGoogleApiClient.disconnect();
        }
        serviceLooper.quit();
    }
}
