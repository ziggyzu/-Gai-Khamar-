package com.gaikhamar.app.ui;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import com.gaikhamar.app.util.LocaleHelper;

/** All screens extend this so the chosen language is applied everywhere. */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    // Keeps AppCompat from overwriting our language with the system one
    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    protected void switchLanguage(String lang) {
        if (lang.equals(LocaleHelper.current(this))) return;
        LocaleHelper.save(this, lang);
        recreate();
    }
}