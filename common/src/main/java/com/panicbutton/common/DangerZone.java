package com.panicbutton.common;

public class DangerZone {

    private final String id;
    private final long latitude;
    private final long longitude;
    private final int radius;

    public DangerZone(String id, long latitude, long longitude, int radius) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }
}
