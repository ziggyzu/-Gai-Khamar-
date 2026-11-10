package com.gaikhamar.app.util;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.gaikhamar.app.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FormDialog {
    private FormDialog() {}

    /** Return true to close the dialog, false to keep it open (for example after an error). */
    public interface OnSave {
        boolean onSave(Values v);
    }

    public static class Values {
        private final Map<String, String> texts = new HashMap<>();
        private final Map<String, Integer> choices = new HashMap<>();

        public String str(String key) {
            String s = texts.get(key);
            return s == null ? "" : s;
        }

        public double num(String key) {
            try {
                return Double.parseDouble(str(key));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public int choice(String key) {
            Integer i = choices.get(key);
            return i == null ? 0 : i;
        }
    }

    private static final int TEXT = 0, NUMBER = 1, DATE = 2, CHOICE = 3, PHONE = 4, PASSWORD = 5;

    private static class Field {
        final String key;
        final int label, kind;
        final String[] options;
        final String preset;

        Field(String key, int label, int kind, String[] options, String preset) {
            this.key = key;
            this.label = label;
            this.kind = kind;
            this.options = options;
            this.preset = preset;
        }
    }

    public static class Builder {
        private final Activity act;
        private final int title;
        private final List<Field> fields = new ArrayList<>();

        public Builder(Activity act, int title) {
            this.act = act;
            this.title = title;
        }

        private Builder add(String key, int label, int kind, String[] options, String preset) {
            fields.add(new Field(key, label, kind, options, preset));
            return this;
        }

        public Builder text(String key, int label) { return add(key, label, TEXT, null, null); }
        public Builder text(String key, int label, String preset) { return add(key, label, TEXT, null, preset); }
        public Builder phone(String key, int label, String preset) { return add(key, label, PHONE, null, preset); }
        public Builder password(String key, int label) { return add(key, label, PASSWORD, null, null); }
        public Builder number(String key, int label) { return add(key, label, NUMBER, null, null); }
        public Builder number(String key, int label, String preset) { return add(key, label, NUMBER, null, preset); }
        public Builder date(String key, int label) { return add(key, label, DATE, null, null); }
        public Builder choice(String key, int label, String[] options) { return add(key, label, CHOICE, options, "0"); }
        public Builder choice(String key, int label, String[] options, int selected) {
            return add(key, label, CHOICE, options, String.valueOf(selected));
        }

        private int dp(int v) {
            return (int) (v * act.getResources().getDisplayMetrics().density);
        }

        public void show(OnSave onSave) {
            LinearLayout box = new LinearLayout(act);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(dp(20), dp(8), dp(20), 0);

            final Map<String, TextInputEditText> edits = new HashMap<>();
            final Map<String, Spinner> spinners = new HashMap<>();

            for (Field f : fields) {
                if (f.kind == CHOICE) {
                    TextView label = new TextView(act);
                    label.setText(f.label);
                    label.setTypeface(null, Typeface.BOLD);
                    label.setPadding(0, dp(12), 0, 0);
                    Spinner sp = new Spinner(act);
                    sp.setAdapter(new ArrayAdapter<>(act, android.R.layout.simple_spinner_dropdown_item, f.options));
                    try {
                        sp.setSelection(Integer.parseInt(f.preset));
                    } catch (NumberFormatException ignored) {
                    }
                    box.addView(label);
                    box.addView(sp, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
                    spinners.put(f.key, sp);
                } else {
                    TextInputLayout til = new TextInputLayout(act, null,
                            com.google.android.material.R.attr.textInputStyle);
                    til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    til.setHint(act.getString(f.label));
                    final TextInputEditText et = new TextInputEditText(til.getContext());
                    if (f.kind == NUMBER) {
                        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    } else if (f.kind == PHONE) {
                        et.setInputType(InputType.TYPE_CLASS_PHONE);
                    } else if (f.kind == PASSWORD) {
                        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else if (f.kind == DATE) {
                        et.setFocusable(false);
                        et.setClickable(true);
                        et.setText(DateUtil.today());
                        et.setOnClickListener(v -> {
                            Calendar c = Calendar.getInstance();
                            new DatePickerDialog(act,
                                    (view, y, m, d) -> et.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                        });
                    } else {
                        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                    }
                    if (f.preset != null) et.setText(f.preset);
                    til.addView(et, new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.topMargin = dp(8);
                    box.addView(til, lp);
                    edits.put(f.key, et);
                }
            }

            ScrollView scroll = new ScrollView(act);
            scroll.addView(box);

            AlertDialog dialog = new MaterialAlertDialogBuilder(act)
                    .setTitle(title)
                    .setView(scroll)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.save, null)
                    .create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                Values vals = new Values();
                for (Field f : fields) {
                    if (f.kind == CHOICE) {
                        vals.choices.put(f.key, spinners.get(f.key).getSelectedItemPosition());
                    } else {
                        TextInputEditText et = edits.get(f.key);
                        vals.texts.put(f.key, et.getText() == null ? "" : et.getText().toString().trim());
                    }
                }
                if (onSave.onSave(vals)) dialog.dismiss();
            });
        }
    }
}