package com.panicbutton.panicbutton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.panicbutton.common.Config;
import com.panicbutton.common.PanicReport;
import com.parse.ConfigCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "UsT15Ykv77V7m663xg7w5rFgaEvjYC9CF57lkV9c", "Y0uuUxxNOLmYifyFztIkueJVJJ0lkQ6bZTetm8P1");

        createObjectTest();
        retrieveObjectTest();

    }

    public void createObjectTest(){

        ParseObject parseObject = new ParseObject(PanicReport.PANIC_REPORT_CLASS);
        //-30.040647, -51.188322
        ParseGeoPoint point = new ParseGeoPoint();
        point.setLatitude(-30.040647);
        point.setLongitude(-51.188322);
        parseObject.put(PanicReport.LOCATION, point);
        parseObject.saveInBackground();


    }

    public void retrieveObjectTest(){

        // test near point  -30.040321, -51.187442
        // test far point -30.043786, -51.183816
        final ParseGeoPoint pointUser = new ParseGeoPoint();
        pointUser.setLatitude(-30.040321);
        pointUser.setLongitude(-51.187442);


        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig parseConfig, ParseException e) {

                if (e != null) return;

                double nearInKm = parseConfig.getDouble(Config.NEAR_IN_KM, 0.2);

                Log.e("NEAR IN KM", ""+nearInKm);

                ParseQuery<ParseObject> query = ParseQuery.getQuery(PanicReport.PANIC_REPORT_CLASS);
                query.whereWithinKilometers(PanicReport.LOCATION, pointUser, nearInKm);

                query.findInBackground(new FindCallback<ParseObject>() {
                       public void done(List<ParseObject> objects, ParseException e) {
                           if (e == null) {
                               Log.e(LOG_TAG, "objects retrieved: " + objects.size());
                           } else {
                               Log.e(LOG_TAG, "error retrieving objects ");
                           }
                       }
                   }
                );



            }
        });
    }

}
