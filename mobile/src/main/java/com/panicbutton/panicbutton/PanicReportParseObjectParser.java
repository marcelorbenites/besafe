package com.panicbutton.panicbutton;

import com.panicbutton.common.PanicReport;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

public class PanicReportParseObjectParser implements ParseObjectParser {

    @Override public ParseObject parse(Object object) {
        PanicReport panicReport;
        if (object instanceof PanicReport) {
            panicReport = (PanicReport) object;
        } else {
            throw new IllegalArgumentException("Must be ParseObject");
        }

        ParseGeoPoint location = new ParseGeoPoint(panicReport.getLatitude(), panicReport.getLongitude());
        ParseObject parseObject = ParseObject.create(PanicReport.PANIC_REPORT_CLASS);
        parseObject.put(PanicReport.LOCATION, location);
        parseObject.put(PanicReport.RADIUS, panicReport.getRadius());
        return parseObject;

    }
}
