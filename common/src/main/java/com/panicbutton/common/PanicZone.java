package com.panicbutton.common;

public class PanicZone extends DangerZone {

    private final long timestamp;

    public PanicZone(String id, long latitude, long longitude, int radius, long timestamp) {
        super(id, latitude, longitude, radius);
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
