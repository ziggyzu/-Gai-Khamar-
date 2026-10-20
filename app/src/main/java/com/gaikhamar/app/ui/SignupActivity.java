package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.User;
import com.gaikhamar.app.databinding.ActivitySignupBinding;
import com.gaikhamar.app.util.PasswordUtil;
import com.gaikhamar.app.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class SignupActivity extends BaseActivity {

    private ActivitySignupBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        Repository repo = Repository.get(this);
        SessionManager session = new SessionManager(this);

        b.tvBack.setOnClickListener(v -> finish());

        b.btnCreate.setOnClickListener(v -> {
            String name = text(b.etName);
            String phone = text(b.etPhone);
            String farm = text(b.etFarmName);
            String code = text(b.etFarmCode).toUpperCase(Locale.US);
            String pw = text(b.etPassword);
            String confirm = text(b.etConfirm);

            if (name.isEmpty() || farm.isEmpty()) {
                toast(R.string.err_name_farm);
            } else if (phone.length() < 10) {
                toast(R.string.err_phone);
            } else if (!code.matches("[A-Z]{2,3}")) {
                toast(R.string.err_farm_code);
            } else if (pw.length() < 6) {
                toast(R.string.err_password_short);
            } else if (!pw.equals(confirm)) {
                toast(R.string.err_password_mismatch);
            } else {
                User u = new User();
                u.name = name;
                u.phone = phone;
                u.farmName = farm;
                u.farmCode = code;
                u.passwordHash = PasswordUtil.hash(pw);
                u.role = "ADMIN";
                u.createdAt = System.currentTimeMillis();

                repo.signup(u, saved -> {
                    if (saved == null) {
                        toast(R.string.err_phone_exists);
                    } else {
                        session.login(saved);
                        Intent i = new Intent(this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                });
            }
        });
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}