package com.panicbutton.panicbutton;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

public class DangerZoneParseObjectParser implements ParseObjectParser {

    @Override public ParseObject parse(Object object) {
        DangerZone dangerZone;
        if (object instanceof DangerZone) {
            dangerZone = (DangerZone) object;
        } else {
            throw new IllegalArgumentException("Must be ParseObject");
        }

        ParseGeoPoint location = new ParseGeoPoint(dangerZone.getLatitude(), dangerZone.getLongitude());
        ParseObject parseObject = ParseObject.create(PanicReport.PANIC_REPORT_CLASS);
        parseObject.put(PanicReport.LOCATION, location);
        parseObject.put(PanicReport.RADIUS, dangerZone.getRadius());
        return parseObject;
    }
}
