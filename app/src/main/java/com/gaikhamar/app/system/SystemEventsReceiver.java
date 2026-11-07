package com.gaikhamar.app.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class SystemEventsReceiver extends BroadcastReceiver {

    public interface Listener {
        void onAirplane(boolean on);
        void onBatteryLow(boolean low);
    }

    private final Listener listener;

    public SystemEventsReceiver(Listener listener) {
        this.listener = listener;
    }

    public static IntentFilter filter() {
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        f.addAction(Intent.ACTION_BATTERY_LOW);
        f.addAction(Intent.ACTION_BATTERY_OKAY);
        return f;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case Intent.ACTION_AIRPLANE_MODE_CHANGED:
                listener.onAirplane(intent.getBooleanExtra("state", false));
                break;
            case Intent.ACTION_BATTERY_LOW:
                listener.onBatteryLow(true);
                break;
            case Intent.ACTION_BATTERY_OKAY:
                listener.onBatteryLow(false);
                break;
            default:
                break;
        }
    }
}