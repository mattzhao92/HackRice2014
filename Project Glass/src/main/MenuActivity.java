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


import java.util.ArrayList;

import lib.EventFetcher;
import lib.ICallBack;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.glass.sample.compass.R;
import com.parse.Parse;

/**
 * This activity manages the options menu that appears when the user taps on the compass's live
 * card.
 */
public class MenuActivity extends Activity {

    private EventService.CompassBinder mCompassService;
    private boolean mResumed;
    private String currentEventId;
    private String currentEventName;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	System.out.println("mConnection onServiceConnected \n");
            if (service instanceof EventService.CompassBinder) {
                mCompassService = (EventService.CompassBinder) service;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Do nothing.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(this, "FiJwGLjq2YlT3ZNS0XR0l4stnsoTznGyBtPGtNQq", "spVdi98WR76oqNwxF2opcyIXa2yo0Oysl3I4zpA6");
        bindService(new Intent(this, EventService.class), mConnection, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        openOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void openOptionsMenu() {
    	if (ARView.eventInSight == null) {
    		super.closeOptionsMenu();
    		closeOptionsMenu();
            unbindService(mConnection);
            finish();
    		return;
    	} else {
    		System.out.println("Lock!!!!!");
    		currentEventId = ARView.eventInSight.getId();
    		currentEventName = ARView.eventInSight.getName();
    	}
        if (mResumed && mCompassService != null && ARView.eventInSight != null) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent;
        switch (item.getItemId()) {
            case R.id.view_summary:
                //mCompassService.readHeadingAloud();
            	intent = new Intent(MenuActivity.this, SummaryActivity.class);
            	startActivity(intent);
                return true;
            case R.id.view_gallery:
            	intent = new Intent(MenuActivity.this, CardScrollActivity.class);
            	startActivity(intent);
                return true;
            case R.id.give_direction:
            	Intent nav_intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q=Houston&mode=w"));
                startActivity(nav_intent);
                return true;
            case R.id.say_review:
            	Intent review_intent = new Intent(MenuActivity.this, ReviewVoiceActivity.class);
            	startActivity(review_intent);
                return true;
            case R.id.upload_snapshot:
            	Intent it = new Intent(MenuActivity.this, CameraActivity.class);
            	Bundle bundle = new Bundle();
            	bundle.putString("eventId", currentEventId);
            	bundle.putString("eventName", currentEventName);
            	it.putExtras(bundle);
            	startActivity(it);
            	return true;
            case R.id.see_reviews:
            	EventFetcher ef = new EventFetcher(getApplicationContext());
            	ef.getReviews(ARView.eventInSight.getId(), new ICallBack() {
					@Override
					public void call(Object... params) {
						@SuppressWarnings("unchecked")
						ArrayList<String> rs = (ArrayList<String>) params[0];
						System.out.println("Rs is " + rs);
						ARView.eventInSight.getReviews().addAll(rs);
						Intent viewIntent = new Intent(MenuActivity.this, ReviewDisplayActivity.class);
						startActivity(viewIntent);
					}
				});
            	
            case R.id.stop:
                //stopService(new Intent(this, EventService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        currentEventId = null;
        currentEventName = null;
        unbindService(mConnection);
        finish();
    }

}
