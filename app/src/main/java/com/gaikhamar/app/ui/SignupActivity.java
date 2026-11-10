package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.gaikhamar.app.R;
import com.gaikhamar.app.cloud.CloudAuth;
import com.gaikhamar.app.cloud.CloudSync;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.User;
import com.gaikhamar.app.databinding.ActivitySignupBinding;
import com.gaikhamar.app.util.LoginIds;
import com.gaikhamar.app.util.PasswordUtil;
import com.gaikhamar.app.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class SignupActivity extends BaseActivity {

    private ActivitySignupBinding b;
    private Repository repo;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = Repository.get(this);
        session = new SessionManager(this);

        b.tvBack.setOnClickListener(v -> finish());
        b.btnGoogle.setOnClickListener(v -> AuthHelper.googleFlow(this, this::finishLogin));
        b.btnCreate.setOnClickListener(v -> create());
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void toast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    private void setBusy(boolean busy) {
        b.btnCreate.setEnabled(!busy);
        b.btnGoogle.setEnabled(!busy);
    }

    private void create() {
        String name = text(b.etName);
        String email = LoginIds.normalize(text(b.etEmail));
        String farm = text(b.etFarmName);
        String code = text(b.etFarmCode).toUpperCase(Locale.US);
        String pw = text(b.etPassword);
        String confirm = text(b.etConfirm);

        if (name.isEmpty() || farm.isEmpty()) { toast(R.string.err_name_farm); return; }
        if (!LoginIds.isEmail(email)) { toast(R.string.err_email); return; }
        if (!code.matches("[A-Z]{2,3}")) { toast(R.string.err_farm_code); return; }
        if (pw.length() < 6) { toast(R.string.err_password_short); return; }
        if (!pw.equals(confirm)) { toast(R.string.err_password_mismatch); return; }

        User u = new User();
        u.name = name;
        u.phone = email;              // the "phone" column holds the login id
        u.farmName = farm;
        u.farmCode = code;
        u.passwordHash = PasswordUtil.hash(pw);
        u.role = "ADMIN";
        u.createdAt = System.currentTimeMillis();

        setBusy(true);
        if (!CloudSync.isOnline(this)) {
            saveLocal(u, true);
            return;
        }
        CloudAuth.create(email, pw, r -> {
            if (r.code == CloudAuth.EXISTS) {
                setBusy(false);
                toast(R.string.err_email_exists);
            } else {
                saveLocal(u, !r.ok());   // not ok = cloud was unreachable, so keep it on this phone for now
            }
        });
    }

    private void saveLocal(User u, boolean offlineOnly) {
        repo.signup(u, saved -> {
            setBusy(false);
            if (saved == null) {
                toast(R.string.err_email_exists);
                return;
            }
            if (offlineOnly) toast(R.string.saved_offline);
            finishLogin(saved);
        });
    }

    private void finishLogin(User user) {
        session.login(user);
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}