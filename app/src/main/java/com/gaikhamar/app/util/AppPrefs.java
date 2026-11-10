package com.gaikhamar.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {
    private final SharedPreferences sp;

    public AppPrefs(Context c) {
        sp = c.getApplicationContext().getSharedPreferences("gk_prefs", Context.MODE_PRIVATE);
    }

    public int district() { return sp.getInt("district", 4); }   // 4 = Dhaka
    public void setDistrict(int i) { sp.edit().putInt("district", i).remove("weather").apply(); }

    public boolean biometric() { return sp.getBoolean("biometric", false); }
    public void setBiometric(boolean on) { sp.edit().putBoolean("biometric", on).apply(); }
    public boolean biometricAsked() { return sp.getBoolean("biometric_asked", false); }
    public void setBiometricAsked(boolean v) { sp.edit().putBoolean("biometric_asked", v).apply(); }

    public long lastSync() { return sp.getLong("last_sync", 0); }
    public void setLastSync(long t) { sp.edit().putLong("last_sync", t).apply(); }

    public void setWeather(WeatherHelper.Result r) {
        sp.edit().putString("weather", r.temp + "|" + r.humidity + "|" + r.rain + "|"
                + r.district + "|" + System.currentTimeMillis()).apply();
    }

    public WeatherHelper.Result weather() {
        String s = sp.getString("weather", null);
        if (s == null) return null;
        String[] p = s.split("\\|");
        if (p.length < 5) return null;
        try {
            WeatherHelper.Result r = new WeatherHelper.Result();
            r.temp = Double.parseDouble(p[0]);
            r.humidity = Double.parseDouble(p[1]);
            r.rain = Double.parseDouble(p[2]);
            r.district = Integer.parseInt(p[3]);
            r.time = Long.parseLong(p[4]);
            return r;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
