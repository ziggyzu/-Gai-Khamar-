package com.gaikhamar.app.util;

import android.app.DatePickerDialog;
import android.content.Context;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

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

    /** Fills the field with today's date and opens a date picker when tapped. */
    public static void dateField(Context context, TextInputEditText e) {
        e.setText(DateUtil.today());
        e.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(context,
                    (view, y, m, d) -> e.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }
}