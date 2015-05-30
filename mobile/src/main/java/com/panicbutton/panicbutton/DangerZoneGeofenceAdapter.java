package com.panicbutton.panicbutton;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;
import java.util.List;

public class DangerZoneGeofenceAdapter {

    public static Geofence toGeofence(DangerZone dangerZone) {
        Geofence.Builder builder = new Geofence.Builder();
        builder.setRequestId(dangerZone.getId())
                .setCircularRegion(dangerZone.getLatitude(), dangerZone.getLongitude(), DistanceUtil.toMetersValue(dangerZone.getRadius()));

        if (dangerZone instanceof PanicZone) {
            builder.setExpirationDuration(((PanicZone) dangerZone).getTimeThreshold());
        } else {
            builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        }
        return builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build();
    }

    public static GeofencingRequest toGeofences(List<DangerZone> dangerZones) {
        GeofencingRequest.Builder geofences = new GeofencingRequest.Builder();
        int size = dangerZones.size();
        for (int i = 0; i < size; i++) {
            geofences.addGeofence(DangerZoneGeofenceAdapter.toGeofence(dangerZones.get(i)));
        }
        return geofences.build();
    }

    public static List<String> toGeofencesId(List<DangerZone> dangerZones) {
        List<String> ids= new ArrayList<String>();
        int size = dangerZones.size();
        for (int i = 0; i < size; i++) {
            ids.add(dangerZones.get(i).getId());
        }
        return ids;
    }

}
