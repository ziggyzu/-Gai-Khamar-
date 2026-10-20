package com.gaikhamar.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.gaikhamar.app.data.entity.User;

public class SessionManager {
    private final SharedPreferences sp;

    public SessionManager(Context context) {
        sp = context.getApplicationContext().getSharedPreferences("gk_session", Context.MODE_PRIVATE);
    }

    public void login(User u) {
        sp.edit()
                .putLong("userId", u.id)
                .putString("name", u.name)
                .putString("farmName", u.farmName)
                .putString("farmCode", u.farmCode)
                .putString("role", u.role)
                .apply();
    }

    public boolean isLoggedIn() { return sp.getLong("userId", -1) != -1; }
    public String getName() { return sp.getString("name", ""); }
    public String getFarmName() { return sp.getString("farmName", ""); }
    public String getFarmCode() { return sp.getString("farmCode", "GK"); }
    public String getRole() { return sp.getString("role", "WORKER"); }
    public void logout() { sp.edit().clear().apply(); }
}