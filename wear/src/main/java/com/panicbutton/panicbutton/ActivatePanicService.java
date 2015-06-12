/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.panicbutton.panicbutton;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Creates a sound on the paired phone to find it.
 */
public class ActivatePanicService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "ActivatePanicService";
    public static final String PATH_ACTIVATE_SERVICE = "/activate_service";
    public static final String ACTION_ACTIVATE_SERVICE = "action_activate_service";
    public static final String FIELD_ACTIVATE_SERVICE = "field_activate_service";

    private GoogleApiClient mGoogleApiClient;

    Intent intentReceived;

    public ActivatePanicService() {
        super(ActivatePanicService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(LOG_TAG, "onHandleIntent");
        intentReceived = intent;

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(LOG_TAG, "onConnected");

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_ACTIVATE_SERVICE);
        putDataMapRequest.getDataMap().putBoolean(FIELD_ACTIVATE_SERVICE, true);
        putDataMapRequest.getDataMap().putLong("data", new Date().getTime());

        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d(LOG_TAG, "Calling start service was successful: " + dataItemResult.getStatus()
                                .isSuccess());
                        Toast.makeText(getApplicationContext(), "Calling start service was successful", Toast.LENGTH_SHORT);
                    }
                });



    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(LOG_TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(LOG_TAG, "onConnectionFailed");
    }

}
