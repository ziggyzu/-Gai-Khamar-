package com.gaikhamar.app.util;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;

import com.gaikhamar.app.R;

public final class BiometricHelper {
    private BiometricHelper() {}

    public interface Result {
        void onSuccess();
        void onFail();
    }

    public static boolean available(Context c) {
        return BiometricManager.from(c).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void prompt(FragmentActivity a, int titleRes, Result cb) {
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(a.getString(titleRes))
                .setNegativeButtonText(a.getString(R.string.cancel))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build();
        BiometricPrompt prompt = new BiometricPrompt(a, ContextCompat.getMainExecutor(a),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult r) {
                        cb.onSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        cb.onFail();
                    }
                });
        prompt.authenticate(info);
    }
}