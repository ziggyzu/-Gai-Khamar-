package com.gaikhamar.app.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.ReminderCounts;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.util.LocaleHelper;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pr = goAsync();
        final Context app = context.getApplicationContext();
        new Thread(() -> {
            try {
                Context lc = LocaleHelper.wrap(app);
                ReminderCounts rc = Repository.get(app).reminderCountsSync();
                String text = ReminderHelper.format(lc, rc);
                if (text != null) {
                    NotificationHelper.createChannels(app);
                    NotificationHelper.notifyReminder(app, 2001, lc.getString(R.string.app_name), text);
                }
            } finally {
                pr.finish();
            }
        }).start();
    }
}