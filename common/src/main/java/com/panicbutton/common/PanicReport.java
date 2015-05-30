package com.panicbutton.common;

public class PanicReport {

    public static final String PANIC_REPORT_CLASS = "PanicReport";
    public static final String LOCATION = "location";


    private long id;
    private long latitude;
    private long longitude;

    public PanicReport(long id, long latitude, long longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PanicReport{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
