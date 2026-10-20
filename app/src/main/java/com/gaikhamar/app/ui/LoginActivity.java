package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.databinding.ActivityLoginBinding;
import com.gaikhamar.app.util.LocaleHelper;
import com.gaikhamar.app.util.SessionManager;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            startMain();
            return;
        }

        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Repository repo = Repository.get(this);

        b.btnEnglish.setOnClickListener(v -> switchLanguage(LocaleHelper.EN));
        b.btnBangla.setOnClickListener(v -> switchLanguage(LocaleHelper.BN));

        b.btnLogin.setOnClickListener(v -> {
            String phone = b.etPhone.getText() == null ? "" : b.etPhone.getText().toString().trim();
            String pw = b.etPassword.getText() == null ? "" : b.etPassword.getText().toString();
            if (phone.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, R.string.err_enter_phone_password, Toast.LENGTH_SHORT).show();
                return;
            }
            repo.login(phone, pw, user -> {
                if (user == null) {
                    Toast.makeText(this, R.string.err_wrong_login, Toast.LENGTH_SHORT).show();
                } else {
                    session.login(user);
                    startMain();
                }
            });
        });

        b.tvSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}