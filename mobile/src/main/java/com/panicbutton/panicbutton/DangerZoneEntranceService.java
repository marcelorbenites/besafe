package com.panicbutton.panicbutton;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class DangerZoneEntranceService extends IntentService {

    private DangerZoneProvider dangerZoneProvider;
    private BeSafeNotificationBuilder notificationBuilder;
    private NotificationManager notificationManager;


    public DangerZoneEntranceService() {
        super("com.panicbutton.android.DangerZoneEntranceService");
    }

    @Override public void onCreate() {
        super.onCreate();
        dangerZoneProvider = InMemoryDangerZoneProvider.getInstance();
        notificationBuilder = new BeSafeNotificationBuilder(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
                        notificationManager.notify(dangerZone.hashCode(),notificationBuilder.build(getString(R.string.service_danger_zone_entrance_alert)));
                        Log.d("PanicButton", String.format("Entered danger zone %s", dangerZone.getId()));
                    }
                }
            }
        }
    }
}
