package com.gaikhamar.app.system;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gaikhamar.app.R;
import com.gaikhamar.app.ui.MainActivity;
import com.gaikhamar.app.util.LocaleHelper;

public final class NotificationHelper {
    public static final String CH_REMINDERS = "reminders";
    public static final String CH_MONITOR = "monitor";

    private NotificationHelper() {}

    public static void createChannels(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        Context lc = LocaleHelper.wrap(c);
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CH_REMINDERS,
                lc.getString(R.string.ch_reminders), NotificationManager.IMPORTANCE_DEFAULT));
        nm.createNotificationChannel(new NotificationChannel(CH_MONITOR,
                lc.getString(R.string.ch_monitor), NotificationManager.IMPORTANCE_LOW));
    }

    public static PendingIntent openApp(Context c) {
        return PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void notifyReminder(Context c, int id, String title, String text) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CH_REMINDERS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text.split("\n")[0])
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(openApp(c))
                .setAutoCancel(true);
        NotificationManagerCompat.from(c).notify(id, b.build());
    }
}