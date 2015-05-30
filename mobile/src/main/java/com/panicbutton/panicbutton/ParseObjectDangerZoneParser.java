package com.panicbutton.panicbutton;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

public class ParseObjectDangerZoneParser implements DangerZoneParser {

    private final int radius;

    public ParseObjectDangerZoneParser(int radius) {
        this.radius = radius;
    }

    @Override public DangerZone parse(Object object) throws IllegalArgumentException {
        ParseObject parseObject;
        if (object instanceof ParseObject) {
            parseObject = (ParseObject) object;
        } else {
            throw new IllegalArgumentException("Must be ParseObject");
        }
        ParseGeoPoint location = (ParseGeoPoint) parseObject.get(DangerZone.LOCATION);
        return new DangerZone(parseObject.getObjectId(), location.getLatitude(), location.getLongitude(), radius);
    }
}
