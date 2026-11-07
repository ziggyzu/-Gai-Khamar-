package com.gaikhamar.app.system;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.FarmProvider;
import com.gaikhamar.app.util.LocaleHelper;
import com.gaikhamar.app.util.NumFmt;

public class FarmMonitorService extends Service {

    public static final String ACTION_STOP = "com.gaikhamar.app.STOP_MONITOR";
    private static final int NOTIF_ID = 1001;
    private static volatile boolean running = false;

    public static boolean isRunning() { return running; }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SystemEventsReceiver receiver;
    private Context lc;
    private boolean airplane, batteryLow;
    private String lastText = "", lastBig = "";

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 60_000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        lc = LocaleHelper.wrap(this);
        NotificationHelper.createChannels(this);
        airplane = Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        startForeground(NOTIF_ID, build(lc.getString(R.string.monitor_starting), null));
        if (!running) {
            running = true;
            receiver = new SystemEventsReceiver(new SystemEventsReceiver.Listener() {
                @Override public void onAirplane(boolean on) { airplane = on; show(); }
                @Override public void onBatteryLow(boolean low) { batteryLow = low; show(); }
            });
            registerReceiver(receiver, SystemEventsReceiver.filter());
            handler.post(tick);
        }
        return START_NOT_STICKY;
    }

    /** Reads the farm summary through our own Content Provider, off the main thread. */
    private void refresh() {
        new Thread(() -> {
            try (Cursor c = getContentResolver().query(FarmProvider.URI_SUMMARY, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int animals = c.getInt(c.getColumnIndexOrThrow("animals"));
                    double milk = c.getDouble(c.getColumnIndexOrThrow("milk_today"));
                    int low = c.getInt(c.getColumnIndexOrThrow("low_stock"));
                    int vacc = c.getInt(c.getColumnIndexOrThrow("vaccines_due"));
                    int calving = c.getInt(c.getColumnIndexOrThrow("calving_soon"));
                    lastText = lc.getString(R.string.monitor_text, String.valueOf(animals), NumFmt.n(milk));
                    lastBig = lc.getString(R.string.monitor_big, String.valueOf(low),
                            String.valueOf(vacc), String.valueOf(calving));
                    handler.post(this::show);
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void show() {
        StringBuilder big = new StringBuilder(lastBig);
        if (airplane) big.append("\n").append(lc.getString(R.string.banner_airplane));
        if (batteryLow) big.append("\n").append(lc.getString(R.string.banner_battery));
        NotificationManagerCompat.from(this).notify(NOTIF_ID, build(lastText, big.toString()));
    }

    private Notification build(String text, String big) {
        Intent stop = new Intent(this, FarmMonitorService.class).setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 1, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, NotificationHelper.CH_MONITOR)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(lc.getString(R.string.monitor_title))
                .setContentText(text)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(NotificationHelper.openApp(this))
                .addAction(0, lc.getString(R.string.monitor_stop_action), stopPi);
        if (big != null && !big.isEmpty()) {
            nb.setStyle(new NotificationCompat.BigTextStyle().bigText(text + "\n" + big));
        }
        return nb.build();
    }

    @Override
    public void onDestroy() {
        running = false;
        handler.removeCallbacks(tick);
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException ignored) {
            }
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}