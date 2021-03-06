/*
 * Copyright (C) 2013 The Android Open Source Project
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

package main;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;
import com.parse.Parse;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import location.OrientationManager;

public class EventService extends Service {

    private static final String LIVE_CARD_ID = "compass";

    public class CompassBinder extends Binder {

    }

    private final CompassBinder mBinder = new CompassBinder();
    private OrientationManager mOrientationManager;
 
    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;
    private ARViewRenderer mRenderer;

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "FiJwGLjq2YlT3ZNS0XR0l4stnsoTznGyBtPGtNQq", "spVdi98WR76oqNwxF2opcyIXa2yo0Oysl3I4zpA6");
        mTimelineManager = TimelineManager.from(this);
        mOrientationManager = new OrientationManager((SensorManager) getSystemService(Context.SENSOR_SERVICE), 
        											 (LocationManager) getSystemService(Context.LOCATION_SERVICE));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);
            mRenderer = new ARViewRenderer(this, mOrientationManager);

            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mRenderer);
            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mLiveCard.publish(PublishMode.REVEAL);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard.getSurfaceHolder().removeCallback(mRenderer);
            mLiveCard = null;
        }

        mOrientationManager = null;
        super.onDestroy();
    }
}
