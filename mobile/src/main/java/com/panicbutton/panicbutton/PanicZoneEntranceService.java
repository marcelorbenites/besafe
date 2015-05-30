package com.panicbutton.panicbutton;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.panicbutton.common.PanicReport;
import com.panicbutton.common.PanicReportProvider;

import java.util.List;

public class PanicZoneEntranceService extends IntentService {

    private PanicReportProvider reportProvider;

    public PanicZoneEntranceService() {
        super("com.useaurea.aurea.internal.geofence.GeofenceEventIntentService");
    }

    @Override public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            List<Geofence> geofenceList = event.getTriggeringGeofences();
            int size = ((geofenceList == null) ? 0 : geofenceList.size());

            PanicReport report;
            for (int i = 0; i < size; i++) {
                report = reportProvider.get(Long.valueOf(geofenceList.get(i).getRequestId()));
                if (report != null) {
                    if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        //TODO warn the user;
                    }
                }
            }
        }
    }
}
