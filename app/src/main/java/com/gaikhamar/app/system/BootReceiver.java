package com.gaikhamar.app.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** After the phone restarts, alarms are lost, so we schedule the daily reminder again. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AlarmScheduler.scheduleDaily(context);
        }
    }
}