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

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;

/**
 * Listens for disconnection from home device.
 */
public class StartListenerService extends WearableListenerService {

    private static final String LOG_TAG = "StartListenerService";

    public static final String PATH_ACTIVATE_SERVICE = "/activate_service";
    public static final String ACTION_ACTIVATE_SERVICE = "action_activate_service";
    public static final String FIELD_ACTIVATE_SERVICE = "field_activate_service";

    private static final String FIELD_ALARM_ON = "alarm_on";

    private static int mOrigVolume;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(LOG_TAG, "ON CREATE");

    }

    @Override
    public void onDestroy() {
        // Reset the alarm volume to the user's original setting.
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        Log.e(LOG_TAG, "ON DATA CHANGED");

        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "onDataChanged: " + dataEvents + " for " + getPackageName());
        }
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.i(LOG_TAG, event + " deleted");
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {

                Boolean turnOn =
                        DataMap.fromByteArray(event.getDataItem().getData()).get(FIELD_ACTIVATE_SERVICE);

                if(turnOn){
                    Intent intent = new Intent(this, DangerZoneMonitorService.class);
                    intent.setAction(DangerZoneMonitorService.ACTION_INITIALIZE);
                    startService(intent);
                }
            }
        }
        dataEvents.close();
    }

}
