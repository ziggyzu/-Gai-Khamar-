package com.gaikhamar.app.util;

import java.util.Locale;

public final class NumFmt {
    private NumFmt() {}

    /** For display: 1,250 or 12.5 (always English digits). */
    public static String n(double v) {
        if (Math.abs(v - Math.rint(v)) < 0.005) {
            return String.format(Locale.US, "%,.0f", v);
        }
        return String.format(Locale.US, "%,.1f", v);
    }

    /** For filling input fields: 1250 or 12.5 (no commas). */
    public static String plain(double v) {
        if (Math.abs(v - Math.rint(v)) < 0.005) {
            return String.valueOf((long) Math.rint(v));
        }
        return String.valueOf(Math.round(v * 100.0) / 100.0);
    }
}