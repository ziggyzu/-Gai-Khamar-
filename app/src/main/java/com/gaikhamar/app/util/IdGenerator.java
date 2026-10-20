package com.gaikhamar.app.util;

import java.util.Calendar;
import java.util.Locale;

public final class IdGenerator {
    private IdGenerator() {}

    /** Example: KH-B-26-0042 = farm KH, Beef, year 2026, animal number 42. */
    public static String make(String farmCode, String category, long sequence) {
        String c = "DAIRY".equals(category) ? "D" : "CALF".equals(category) ? "C" : "B";
        int yy = Calendar.getInstance().get(Calendar.YEAR) % 100;
        return String.format(Locale.US, "%s-%s-%02d-%04d", farmCode, c, yy, sequence);
    }
}