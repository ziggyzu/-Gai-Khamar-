package com.gaikhamar.app.system;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public final class AlarmScheduler {
    private AlarmScheduler() {}

    /** Daily reminder around 7:00. Safe to call many times: it replaces the same alarm. */
    public static void scheduleDaily(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(c, 7, new Intent(c, ReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (cal.before(Calendar.getInstance())) cal.add(Calendar.DAY_OF_MONTH, 1);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }
}