package com.panicbutton.common;

public class PanicReport {

    public static final String PANIC_REPORT_CLASS = "PanicReport";
    public static final String LOCATION = "location";

    private final int radius;
    private final double latitude;
    private final double longitude;

    public PanicReport(double latitude, double longitude, int radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
