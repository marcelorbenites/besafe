package com.panicbutton.panicbutton;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.panicbutton.common.DangerZone;
import com.panicbutton.common.InMemoryDangerZoneProvider;
import com.panicbutton.common.PanicReport;
import com.panicbutton.common.DangerZoneProvider;

import java.util.List;

public class DangerZoneMonitorService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int GEOFENCE_UPDATE = 0;
    public static final int GEOFENCE_STOP = 1;

    public static final String ACTION_GEOFENCE_INITIALIZE = "com.panicbutton.android.action.GEOFENCE_INITIALIZE";
    public static final String ACTION_GEOFENCE_STOP = "com.panicbutton.android.action.GEOFENCE_STOP";
    public static final String ACTION_GEOFENCE_UPDATE = "com.panicbutton.android.action.GEOFENCE_UPDATE";

    private volatile Looper serviceLooper;
    private volatile ServiceHandler serviceHandler;

    private LocationRequest locationRequest;
    private DangerZoneProvider dangerZoneProvider;
    private Intent intent;
    private GoogleApiClient googleApiClient;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case GEOFENCE_UPDATE:
                    List<DangerZone> newZones = (List) msg.obj;
                    addDangerZones(newZones);
                    List<DangerZone> oldGeoFences = dangerZoneProvider.getAll();
                    oldGeoFences.removeAll(newZones);
                    removeDangerZones(oldGeoFences);
                    break;
                case GEOFENCE_STOP:
                    removeDangerZones(dangerZoneProvider.getAll());
                    break;
                default:
                    break;
            }
            stopSelf();
        }

        private void addDangerZones(List<DangerZone> dangerZones) {
            if (dangerZones.size() > 0) {
                persistDangerZones(dangerZones);
                startMonitoringDangerZones(dangerZones);
            }
        }

        private void removeDangerZones(List<DangerZone> dangerZones) {
            if (dangerZones.size() > 0) {
                deleteGeofences(dangerZones);
                stopMonitoringDangerZones(dangerZones);
            }
        }
    }

    @Override public void onCreate() {

        final HandlerThread thread = new HandlerThread("IntentService[com.panicbutton.android.internal.geofence.PanicZoneMonitorService]");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        dangerZoneProvider = new InMemoryDangerZoneProvider();

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setFastestInterval(30 * 1000);
        locationRequest.setInterval(300 * 1000);
        locationRequest.setSmallestDisplacement(1000);


        // panicReportProvider = Factory???
        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                startAction(intent);
            } else if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onConnected(Bundle bundle) {
        startAction(intent);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        stopSelf();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopSelf();
    }

    private void startAction(Intent intent) {
        if (intent != null) {
            if (ACTION_GEOFENCE_INITIALIZE.equals(intent.getAction())) {
                requestLocationUpdates(locationRequest);
            } else if (ACTION_GEOFENCE_UPDATE.equals(intent.getAction())) {
                Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
                if (location != null) {
                    loadNewPanicZones(location);
                }
            } else if (ACTION_GEOFENCE_STOP.equals(intent.getAction())) {
                removeLocationUpdatesRequest();
            }
        }
    }

    private void removeLocationUpdatesRequest() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, getGeofencePendingIntent(DangerZoneMonitorService.class, DangerZoneMonitorService.ACTION_GEOFENCE_UPDATE)).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    startActionInBackground(GEOFENCE_STOP);
                }
            }
        });
    }

    private void requestLocationUpdates(final LocationRequest locationRequest) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, getGeofencePendingIntent(DangerZoneMonitorService.class, DangerZoneMonitorService.ACTION_GEOFENCE_UPDATE)).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d("PanicButton", "Updating Panic Zones!");
                }
            }
        });
    }

    private void loadNewPanicZones(Location location) {
        if (location != null) {
            // TODO Load panic zones
        }
    }

    private void startMonitoringDangerZones(List<DangerZone> dangerZones) {
        final int size = dangerZones.size();
        if (size > 0) {
            Status status = LocationServices.GeofencingApi.addGeofences(googleApiClient, DangerZoneGeofenceAdapter.toGeofecens(dangerZones), getGeofencePendingIntent(DangerZoneEntranceService.class)).await();
            if (status.isSuccess()) {
                Log.d("PanicButton", String.format("Started monitoring %d panic zones.", size));
            }
        } else {
            Log.d("PanicButton", "No geofences to monitor.");
        }
    }

    private void persistDangerZones(List<DangerZone> dangerZones) {
        for(DangerZone dangerZone: dangerZones) {
            dangerZoneProvider.insert(dangerZone);
        }
    }

    private void stopMonitoringDangerZones(List<DangerZone> dangerZones) {
        final int size = dangerZones.size();
        if (size > 0) {
            Status status = LocationServices.GeofencingApi.removeGeofences(googleApiClient, DangerZoneGeofenceAdapter.toGeofecensId(dangerZones)).await();
            if (status.isSuccess()) {
                Log.d("PanicButton", String.format("Stopped monitoring %d panic zones.", dangerZones.size()));
            } else {
                Log.d("PanicButton", String.format("Failed stop monitoring panic zoneswith status %s.", status.toString()));

            }
        } else {
            Log.d("PanicButton", "No panic zones being monitored.");
        }
    }

    private void deleteGeofences(List<DangerZone> dangerZones) {
        int size = dangerZones.size();
        for (int i = 0; i < size; i++) {
            dangerZoneProvider.delete(dangerZones.get(i).getId());
        }
        Log.d("PanicButton", String.format("Removed %d panic zones.", dangerZones.size()));
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
        Log.d("PanicButton", "Panic zone manager finished.");
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        serviceLooper.quit();
    }
}
