package com.gaikhamar.app.util;

import android.content.Context;

import com.gaikhamar.app.R;

public final class LabelHelper {
    private LabelHelper() {}

    public static String category(Context c, String code) {
        if ("DAIRY".equals(code)) return c.getString(R.string.dairy);
        if ("CALF".equals(code)) return c.getString(R.string.calf);
        return c.getString(R.string.beef);
    }

    public static String gender(Context c, String code) {
        return "MALE".equals(code) ? c.getString(R.string.male) : c.getString(R.string.female);
    }

    public static String dairyStatus(Context c, String code) {
        if (code == null) return "";
        switch (code) {
            case "HEIFER":   return c.getString(R.string.ds_heifer);
            case "MILKING":  return c.getString(R.string.ds_milking);
            case "DRY":      return c.getString(R.string.ds_dry);
            case "PREGNANT": return c.getString(R.string.pregnant);
            default:         return code;
        }
    }

    public static String health(Context c, String code) {
        if (code == null) return "";
        switch (code) {
            case "HEALTHY":     return c.getString(R.string.h_healthy);
            case "OBSERVATION": return c.getString(R.string.h_observation);
            case "TREATMENT":   return c.getString(R.string.h_treatment);
            case "RECOVERING":  return c.getString(R.string.h_recovering);
            case "QUARANTINE":  return c.getString(R.string.h_quarantine);
            default:            return code;
        }
    }

    public static String status(Context c, String code) {
        if (code == null) return "";
        switch (code) {
            case "ACTIVE": return c.getString(R.string.st_active);
            case "SOLD":   return c.getString(R.string.st_sold);
            case "DEAD":   return c.getString(R.string.st_dead);
            default:       return code;
        }
    }
    public static String feedGroup(Context c, String code) {
        if (code == null) return "";
        switch (code) {
            case "BEEF":    return c.getString(R.string.g_beef);
            case "MILKING": return c.getString(R.string.g_milking);
            case "DRY":     return c.getString(R.string.g_dry);
            case "CALF":    return c.getString(R.string.g_calf);
            default:        return code;
        }
    }
}