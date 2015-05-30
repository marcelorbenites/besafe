package com.panicbutton.panicbutton;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.panicbutton.common.DangerZone;
import com.panicbutton.common.DangerZoneProvider;
import com.panicbutton.common.InMemoryDangerZoneProvider;

import java.util.List;

public class DangerZoneEntranceService extends IntentService {

    private DangerZoneProvider dangerZoneProvider;

    public DangerZoneEntranceService() {
        super("com.panicbutton.android.DangerZoneEntranceService");
    }

    @Override public void onCreate() {
        super.onCreate();
        dangerZoneProvider = InMemoryDangerZoneProvider.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            Log.d("PanicButton", String.format("Received geofence intent %s", event.getGeofenceTransition()));

            List<Geofence> geofenceList = event.getTriggeringGeofences();
            int size = ((geofenceList == null) ? 0 : geofenceList.size());

            DangerZone dangerZone;
            for (int i = 0; i < size; i++) {
                dangerZone = dangerZoneProvider.get(geofenceList.get(i).getRequestId());
                if (dangerZone != null) {
                    if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        Log.d("PanicButton", String.format("Entered danger zone %s", dangerZone.getId()));
                    }
                }
            }
        }
    }
}
