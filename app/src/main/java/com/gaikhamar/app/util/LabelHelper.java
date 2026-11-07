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
    public static String milkSession(Context c, String code) {
        return "EVENING".equals(code) ? c.getString(R.string.evening) : c.getString(R.string.morning);
    }

    public static String breedingType(Context c, String code) {
        if (code == null) return "";
        switch (code) {
            case "HEAT":               return c.getString(R.string.bt_heat);
            case "INSEMINATION":       return c.getString(R.string.bt_insemination);
            case "PREGNANCY_POSITIVE": return c.getString(R.string.bt_preg_pos);
            case "PREGNANCY_NEGATIVE": return c.getString(R.string.bt_preg_neg);
            default:                   return code;
        }
    }
    private static String fromArray(Context c, int codesRes, int labelsRes, String code) {
        String[] codes = c.getResources().getStringArray(codesRes);
        String[] labels = c.getResources().getStringArray(labelsRes);
        for (int i = 0; i < codes.length && i < labels.length; i++) {
            if (codes[i].equals(code)) return labels[i];
        }
        return code == null ? "" : code;
    }

    public static String condition(Context c, String code) {
        return fromArray(c, R.array.condition_codes, R.array.condition_labels, code);
    }

    public static String role(Context c, String code) {
        return fromArray(c, R.array.role_codes, R.array.role_labels, code);
    }

    public static String finCategory(Context c, String code) {
        String s = fromArray(c, R.array.income_codes, R.array.income_labels, code);
        if (s.equals(code)) s = fromArray(c, R.array.expense_codes, R.array.expense_labels, code);
        return s;
    }
}