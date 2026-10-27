package com.gaikhamar.app.util;

import com.google.android.material.textfield.TextInputEditText;

public final class InputUtil {
    private InputUtil() {}

    public static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    public static double num(TextInputEditText e) {
        try {
            return Double.parseDouble(text(e));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}