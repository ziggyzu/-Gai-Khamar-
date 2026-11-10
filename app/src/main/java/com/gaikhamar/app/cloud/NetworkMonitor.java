package com.gaikhamar.app.cloud;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;

public class NetworkMonitor {

    public interface OnOnline {
        void onOnline();
    }

    private ConnectivityManager cm;
    private ConnectivityManager.NetworkCallback callback;

    public void start(Context c, OnOnline listener) {
        stop();
        cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                new Handler(Looper.getMainLooper()).post(listener::onOnline);
            }
        };
        cm.registerDefaultNetworkCallback(callback);
    }

    public void stop() {
        if (cm != null && callback != null) {
            try {
                cm.unregisterNetworkCallback(callback);
            } catch (IllegalArgumentException ignored) {
            }
        }
        callback = null;
    }
}
