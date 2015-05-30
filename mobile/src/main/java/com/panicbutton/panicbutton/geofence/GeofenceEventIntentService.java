package com.panicbutton.panicbutton.geofence;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.useaurea.aurea.AureaBusinessLocation;
import com.useaurea.aurea.AureaManager;
import com.useaurea.aurea.LogLevel;
import com.useaurea.aurea.internal.AureaApplicationResolver;
import com.useaurea.aurea.internal.data.AureaContract;
import com.useaurea.aurea.internal.data.preferences.SharedPreferencesProviderFactory;
import com.useaurea.aurea.internal.log.Log;
import com.useaurea.aurea.internal.log.LogFactory;
import com.useaurea.aurea.internal.networking.ContentHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofenceEventIntentService extends IntentService {

    private AureaBusinessLocationProvider geofenceProvider;
    private AureaApplicationResolver applicationResolver;
    private Log log;

    public GeofenceEventIntentService() {
        super("com.useaurea.aurea.internal.geofence.GeofenceEventIntentService");
    }

    @SuppressWarnings("unchecked") @Override public void onCreate() {
        final SharedPreferencesProviderFactory providerFactory = new SharedPreferencesProviderFactory(this);
        final LogFactory logFactory = new LogFactory(providerFactory.getConfigurationProvider());
        final ContentHelper contentHelper = new ContentHelper(new AureaApplicationResolver(getPackageName()));
        geofenceProvider = new AureaBusinessLocationProvider(getContentResolver(), contentHelper, logFactory.newLog("UseAureaProvider"));
        applicationResolver = new AureaApplicationResolver(getPackageName());
        log = logFactory.newLog("UseAureaGeofence");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            List<Geofence> geofenceList = event.getTriggeringGeofences();
            int size = ((geofenceList == null) ? 0 : geofenceList.size());

            Map<String, String> query;
            List<AureaBusinessLocation> result;
            for (int i = 0; i < size; i++) {

                query = new HashMap<>();
                query.put(AureaContract.AureaBusinessLocationsColumns.LOCATION_ID, geofenceList.get(i).getRequestId());
                result = geofenceProvider.get(query);

                Intent actionIntent = null;
                if (!result.isEmpty()) {
                    if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        actionIntent = new Intent(AureaManager.ACTION_DID_ENTER_AUREA_BUSINESS_LOCATION_FENCE).putExtra(AureaManager.EXTRA_AUREA_BUSINESS_LOCATION, result.get(0));
                    } else if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {
                        actionIntent = new Intent(AureaManager.ACTION_DID_EXIT_AUREA_BUSINESS_LOCATION_FENCE).putExtra(AureaManager.EXTRA_AUREA_BUSINESS_LOCATION, result.get(0));
                    }
                } else {
                    if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        log.internalLog(String.format("ENTERED: Null access to %s", geofenceList.get(i).getRequestId()), LogLevel.DEBUG);
                    }
                }

                if (actionIntent != null) {
                    sendOrderedBroadcast(actionIntent, applicationResolver.resolvePermission(AureaApplicationResolver.AUREA_UPDATE_PERMISSION_SUFFIX));
                }
            }
        }
    }
}
