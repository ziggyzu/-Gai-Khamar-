package com.gaikhamar.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtil {
    private DateUtil() {}

    private static SimpleDateFormat fmt() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US); }
    private static SimpleDateFormat monthFmt() { return new SimpleDateFormat("yyyy-MM", Locale.US); }

    public static String today() { return fmt().format(new Date()); }

    private static Calendar parse(String date) {
        Calendar c = Calendar.getInstance();
        try {
            Date d = fmt().parse(date);
            if (d != null) c.setTime(d);
        } catch (ParseException ignored) {
        }
        return c;
    }

    public static String plusDays(String date, int days) {
        Calendar c = parse(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        return fmt().format(c.getTime());
    }

    public static long daysBetween(String from, String to) {
        long ms = parse(to).getTimeInMillis() - parse(from).getTimeInMillis();
        return Math.round(ms / 86400000.0);
    }

    /** "yyyy-MM" for this month plus an offset in months (0 = this month, -1 = last month). */
    public static String monthKey(int offset) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, offset);
        return monthFmt().format(c.getTime());
    }

    public static String shiftMonth(String key, int delta) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        try {
            Date d = monthFmt().parse(key);
            if (d != null) c.setTime(d);
        } catch (ParseException ignored) {
        }
        c.add(Calendar.MONTH, delta);
        return monthFmt().format(c.getTime());
    }

    /** Oldest first, ending with this month. */
    public static String[] lastMonthKeys(int n) {
        String[] k = new String[n];
        for (int i = 0; i < n; i++) k[i] = monthKey(-(n - 1 - i));
        return k;
    }
}