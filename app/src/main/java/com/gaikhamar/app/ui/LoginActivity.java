package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.gaikhamar.app.R;
import com.gaikhamar.app.cloud.CloudAuth;
import com.gaikhamar.app.cloud.CloudSync;
import com.gaikhamar.app.data.ExtraRepository;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.User;
import com.gaikhamar.app.databinding.ActivityLoginBinding;
import com.gaikhamar.app.util.AppPrefs;
import com.gaikhamar.app.util.BiometricHelper;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.LocaleHelper;
import com.gaikhamar.app.util.LoginIds;
import com.gaikhamar.app.util.PasswordUtil;
import com.gaikhamar.app.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding b;
    private SessionManager session;
    private AppPrefs prefs;
    private Repository repo;
    private ExtraRepository extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        prefs = new AppPrefs(this);
        repo = Repository.get(this);
        extra = ExtraRepository.get(this);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.btnEnglish.setOnClickListener(v -> switchLanguage(LocaleHelper.EN));
        b.btnBangla.setOnClickListener(v -> switchLanguage(LocaleHelper.BN));
        b.btnLogin.setOnClickListener(v -> attemptLogin());
        b.btnGoogle.setOnClickListener(v -> AuthHelper.googleFlow(this, this::onLoggedIn));
        b.tvForgot.setOnClickListener(v -> forgotPassword());
        b.tvSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));

        // Already logged in: ask for the fingerprint if it is turned on
        if (session.isLoggedIn()) {
            if (prefs.biometric() && BiometricHelper.available(this)) {
                BiometricHelper.prompt(this, R.string.bio_title, new BiometricHelper.Result() {
                    @Override public void onSuccess() { startMain(); }
                    @Override public void onFail() { /* stay here so the password can be used */ }
                });
            } else {
                startMain();
            }
        }
    }

    private String text(com.google.android.material.textfield.TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void toast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    private void setBusy(boolean busy) {
        b.btnLogin.setEnabled(!busy);
        b.btnGoogle.setEnabled(!busy);
    }

    private void attemptLogin() {
        String id = LoginIds.normalize(text(b.etLogin));
        String pw = b.etPassword.getText() == null ? "" : b.etPassword.getText().toString();
        if (id.isEmpty() || pw.isEmpty()) {
            toast(R.string.err_login_id);
            return;
        }
        setBusy(true);
        repo.login(id, pw, local -> {                    // local check first
            if (!id.contains("@")) {                     // phone login: staff or older accounts, local only
                setBusy(false);
                if (local == null) toast(R.string.err_wrong_login); else onLoggedIn(local);
            } else {
                emailLogin(id, pw, local);
            }
        });
    }

    /** Online: Firebase decides. Offline: the password saved on this phone is used. */
    private void emailLogin(String email, String pw, User local) {
        if (!CloudSync.isOnline(this)) {
            setBusy(false);
            if (local != null) onLoggedIn(local); else toast(R.string.err_wrong_login);
            return;
        }
        CloudAuth.signIn(email, pw, r -> {
            if (r.ok()) {
                if (local != null) {                      // keep the saved password in step with the cloud
                    local.passwordHash = PasswordUtil.hash(pw);
                    extra.saveAdmin(local, this::onLoggedIn);
                } else {                                  // new phone: ask for the farm details
                    setBusy(false);
                    AuthHelper.askFarmAndCreate(this, email, r.name, PasswordUtil.hash(pw), this::onLoggedIn);
                }
            } else if (r.code == CloudAuth.WRONG) {
                if (local != null) {                      // account made offline: create it in the cloud now
                    CloudAuth.create(email, pw, r2 -> {
                        if (r2.code == CloudAuth.EXISTS || r2.code == CloudAuth.WRONG) {
                            setBusy(false);
                            toast(R.string.err_wrong_login);
                        } else {
                            onLoggedIn(local);
                        }
                    });
                } else {
                    setBusy(false);
                    toast(R.string.err_wrong_login);
                }
            } else {                                      // network trouble: fall back to this phone
                setBusy(false);
                if (local != null) onLoggedIn(local); else AuthHelper.toastError(this, r);
            }
        });
    }

    private void onLoggedIn(User user) {
        setBusy(false);
        session.login(user);
        offerFingerprintThenContinue();
    }

    private void forgotPassword() {
        String typed = LoginIds.normalize(text(b.etLogin));
        new FormDialog.Builder(this, R.string.forgot_password)
                .text("email", R.string.email, typed.contains("@") ? typed : "")
                .show(v -> {
                    String email = LoginIds.normalize(v.str("email"));
                    if (!LoginIds.isEmail(email)) {
                        toast(R.string.err_email);
                        return false;
                    }
                    CloudAuth.sendReset(email, ok -> toast(R.string.reset_sent));
                    return true;
                });
    }

    private void offerFingerprintThenContinue() {
        if (BiometricHelper.available(this) && !prefs.biometric() && !prefs.biometricAsked()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.bio_offer_title)
                    .setMessage(R.string.bio_offer)
                    .setCancelable(false)
                    .setNegativeButton(R.string.no, (d, w) -> {
                        prefs.setBiometricAsked(true);
                        startMain();
                    })
                    .setPositiveButton(R.string.yes, (d, w) ->
                            BiometricHelper.prompt(this, R.string.bio_title, new BiometricHelper.Result() {
                                @Override public void onSuccess() {
                                    prefs.setBiometric(true);
                                    prefs.setBiometricAsked(true);
                                    startMain();
                                }
                                @Override public void onFail() { startMain(); }
                            }))
                    .show();
        } else {
            startMain();
        }
    }

    private void startMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}