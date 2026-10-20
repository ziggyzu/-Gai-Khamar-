package com.gaikhamar.app.util;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public final class LocaleHelper {
    public static final String EN = "en";
    public static final String BN = "bn";

    private static final String PREF = "gk_lang";
    private static final String KEY = "lang";

    private LocaleHelper() {}

    /** Wraps a context so it uses the saved language (or the phone language if none saved). */
    public static Context wrap(Context base) {
        String lang = saved(base);
        if (lang == null) return base;
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        return base.createConfigurationContext(config);
    }

    public static void save(Context c, String lang) {
        c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(KEY, lang).apply();
    }

    public static String current(Context c) {
        String s = saved(c);
        if (s != null) return s;
        return c.getResources().getConfiguration().getLocales().get(0).getLanguage();
    }

    private static String saved(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY, null);
    }
}