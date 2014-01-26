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


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
        System.out.println("coa ni ma");
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
        if (mResumed && mCompassService != null) {
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
        switch (item.getItemId()) {
            case R.id.view_summary:
                //mCompassService.readHeadingAloud();
                return true;
            case R.id.view_gallery:
            	Intent intent = new Intent(MenuActivity.this, CardScrollActivity.class);
            	startActivity(intent);
                return true;
            case R.id.upload_snapshot:
            	Intent it = new Intent(MenuActivity.this, CameraActivity.class);
            	Bundle bundle = new Bundle();
            	// TODO
            	bundle.putString("eventId", "omFao4nmfv");
            	bundle.putString("eventName", "CS Club");
            	it.putExtras(bundle);
            	startActivity(it);
//            	takePicture();
            	return true;
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
        unbindService(mConnection);
        finish();
    }

}
