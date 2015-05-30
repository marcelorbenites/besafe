package com.panicbutton.common;

public class PanicReport {

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
}
