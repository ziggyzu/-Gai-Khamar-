package com.gaikhamar.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtil {
    private DateUtil() {}

    private static SimpleDateFormat fmt() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    public static String today() {
        return fmt().format(new Date());
    }

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

    /** Whole days from one date to another (negative if "to" is earlier). */
    public static long daysBetween(String from, String to) {
        long ms = parse(to).getTimeInMillis() - parse(from).getTimeInMillis();
        return Math.round(ms / 86400000.0);
    }
}