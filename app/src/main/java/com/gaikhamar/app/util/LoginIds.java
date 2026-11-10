package com.gaikhamar.app.util;

import android.util.Patterns;

import java.util.Locale;

/** One saved "login id" per user: a lowercase email, or phone digits for staff. */
public final class LoginIds {
    private LoginIds() {}

    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.contains("@")) return s.toLowerCase(Locale.US);
        return s.replaceAll("[^0-9]", "");
    }

    public static boolean isEmail(String id) {
        return id != null && Patterns.EMAIL_ADDRESS.matcher(id).matches();
    }
}