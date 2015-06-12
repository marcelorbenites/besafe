package com.panicbutton.panicbutton;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by douglasritter on 5/30/15.
 */
public class ActivateActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);
        ButterKnife.inject(this);

    }

    @OnClick(R.id.button)
    public void activateService(View view){

        Intent activateService = new Intent(this, ActivatePanicService.class);
        activateService.setAction(ActivatePanicService.ACTION_ACTIVATE_SERVICE);
        startService(activateService);

    }


}
