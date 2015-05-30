package com.panicbutton.panicbutton;

public class PanicZone extends DangerZone {

    private final long timeThreshold;

    public PanicZone(String id, long latitude, long longitude, int radius, long timeThreshold) {
        super(id, latitude, longitude, radius);
        this.timeThreshold = timeThreshold;
    }

    public long getTimeThreshold() {
        return timeThreshold;
    }
}
