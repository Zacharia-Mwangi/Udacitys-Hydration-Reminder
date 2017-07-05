package com.example.zack.hydrationreminder;
/*
 * Copyright (C) 2016 The Android Open Source Project
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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zack.hydrationreminder.settings_utilities.SettingsActivity;
import com.example.zack.hydrationreminder.sync.ReminderTasks;
import com.example.zack.hydrationreminder.sync.WaterReminderIntentService;
import com.example.zack.hydrationreminder.utilities.NotificationUtils;
import com.example.zack.hydrationreminder.utilities.PreferenceUtilities;
import com.example.zack.hydrationreminder.sync.ReminderUtilities;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mWaterCountDisplay;
    private TextView mChargingCountDisplay;
    private ImageView mChargingImageView;
    private BroadcastReceiver mChargingReceiver;

    private Toast mToast;
    IntentFilter mChargingIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


/** Get the views **/

        mWaterCountDisplay = (TextView) findViewById(R.id.tv_water_count);
        mChargingCountDisplay = (TextView) findViewById(R.id.tv_charging_reminder_count);
        mChargingImageView = (ImageView) findViewById(R.id.iv_power_increment);


/** Set the original values in the UI **/

        updateWaterCount();
        updateChargingReminderCount();
        // COMPLETED (23) Schedule the charging reminder
        ReminderUtilities.scheduleChargingReminder(this);


/** Setup the shared preference listener **/

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // instantiate intent filter: Specify type of intents which an activity can respond to.
        mChargingIntentFilter = new IntentFilter();
        mChargingIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        mChargingIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        mChargingReceiver = new ChargingBroadcastReceiver();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // COMPLETED (2) Get a BatteryManager instance using getSystemService()
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            // COMPLETED (3) Call isCharging on the battery manager and pass the result on to your show
            // charging method
            showCharging(batteryManager.isCharging());
        } else {
            // COMPLETED (4) If your user is not on M+, then...

            // COMPLETED (5) Create a new intent filter with the action ACTION_BATTERY_CHANGED. This is a
            // sticky broadcast that contains a lot of information about the battery state.
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            // COMPLETED (6) Set a new Intent object equal to what is returned by registerReceiver, passing in null
            // for the receiver. Pass in your intent filter as well. Passing in null means that you're
            // getting the current state of a sticky broadcast - the intent returned will contain the
            // battery information you need.
            Intent currentBatteryStatusIntent = registerReceiver(null, ifilter);
            // COMPLETED (7) Get the integer extra BatteryManager.EXTRA_STATUS. Check if it matches
            // BatteryManager.BATTERY_STATUS_CHARGING or BatteryManager.BATTERY_STATUS_FULL. This means
            // the battery is currently charging.
            int batteryStatus = currentBatteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                    batteryStatus == BatteryManager.BATTERY_STATUS_FULL;
            // COMPLETED (8) Update the UI using your showCharging method
            showCharging(isCharging);
        }


/** Register the receiver for future state changes **/

        registerReceiver(mChargingReceiver, mChargingIntentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(mChargingReceiver);
    }


/**
     * Updates the TextView to display the new water count from SharedPreferences
     */


    private void updateWaterCount() {
        int waterCount = PreferenceUtilities.getWaterCount(this);
        mWaterCountDisplay.setText(waterCount+"");

    }




/**
     * Updates the TextView to display the new charging reminder count from SharedPreferences
     */

    private void updateChargingReminderCount() {
        int chargingReminders = PreferenceUtilities.getChargingReminderCount(this);
        String formattedChargingReminders = getResources().getQuantityString(
                R.plurals.charge_notification_count, chargingReminders, chargingReminders);
        mChargingCountDisplay.setText(formattedChargingReminders);

    }
    // change color when charging/not charging
    private void showCharging(boolean isCharging){
        if (isCharging){
            mChargingImageView.setImageResource(R.drawable.ic_power_pink_80px);
        } else {
            mChargingImageView.setImageResource(R.drawable.ic_power_grey_80px);
        }
    }


/**
     * Adds one to the water count and shows a toast
     */

    public void incrementWater(View view) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, R.string.water_chug_toast, Toast.LENGTH_SHORT);
        mToast.show();

        Intent incrementWaterCountIntent = new Intent(this, WaterReminderIntentService.class);
        incrementWaterCountIntent.setAction(ReminderTasks.ACTION_INCREMENT_WATER_COUNT);
        startService(incrementWaterCountIntent);
    }

    // COMPLETED (24) Remove the button and testNotification code
    public void testNotification(View view) {
        NotificationUtils.remindUserBecauseCharging(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

/** Cleanup the shared preference listener **/

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }


/**
     * This is a listener that will update the UI when the water count or charging reminder counts
     * change
     */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceUtilities.KEY_WATER_COUNT.equals(key)) {
            updateWaterCount();
        } else if (PreferenceUtilities.KEY_CHARGING_REMINDER_COUNT.equals(key)) {
            updateChargingReminderCount();
        }
    }

    private class ChargingBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean isCharging = (action.equals(Intent.ACTION_POWER_CONNECTED));
            showCharging(isCharging);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hydration_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        /*if (id == R.id.action_settings_menu){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        } else*/ if(id == R.id.leader_board){
            Intent startLeaderBoard = new Intent(this, Leader_board.class);
            startActivity(startLeaderBoard);
            return true;
        } else if(id == R.id.water_points){
            Intent startWaterPoints = new Intent(this, WaterPoints.class);
            startActivity(startWaterPoints);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


