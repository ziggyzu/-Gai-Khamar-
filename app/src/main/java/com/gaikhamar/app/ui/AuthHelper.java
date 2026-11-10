package com.gaikhamar.app.ui;

import android.content.Context;
import android.widget.Toast;

import com.gaikhamar.app.R;
import com.gaikhamar.app.cloud.CloudAuth;
import com.gaikhamar.app.data.ExtraRepository;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.User;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.LoginIds;

import java.util.Locale;

/** Shared by the login and signup screens. */
final class AuthHelper {
    private AuthHelper() {}

    /** Google button: signs in, then finds or creates the local admin account for that email. */
    static void googleFlow(BaseActivity a, Repository.Callback<User> done) {
        CloudAuth.signInGoogle(a, r -> {
            if (!r.ok()) {
                toastError(a, r);
                return;
            }
            String email = LoginIds.normalize(r.email);
            ExtraRepository.get(a).findUser(email, existing -> {
                if (existing != null) done.onResult(existing);
                else askFarmAndCreate(a, email, r.name, "", done);
            });
        });
    }

    /** Asks for the farm details on a phone that does not know this account yet. */
    static void askFarmAndCreate(BaseActivity a, String email, String displayName,
                                 String passwordHash, Repository.Callback<User> done) {
        new FormDialog.Builder(a, R.string.setup_title)
                .text("name", R.string.full_name, displayName == null ? "" : displayName)
                .text("farm", R.string.farm_name, "")
                .text("code", R.string.farm_code, "")
                .show(v -> {
                    String code = v.str("code").toUpperCase(Locale.US);
                    if (v.str("name").isEmpty() || v.str("farm").isEmpty()) {
                        Toast.makeText(a, R.string.err_name_farm, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!code.matches("[A-Z]{2,3}")) {
                        Toast.makeText(a, R.string.err_farm_code, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    User u = new User();
                    u.name = v.str("name");
                    u.phone = email;
                    u.farmName = v.str("farm");
                    u.farmCode = code;
                    u.passwordHash = passwordHash;
                    u.role = "ADMIN";
                    u.createdAt = System.currentTimeMillis();
                    ExtraRepository.get(a).saveAdmin(u, done::onResult);
                    return true;
                });
    }

    static void toastError(Context c, CloudAuth.Result r) {
        String msg;
        if (r.code == CloudAuth.NETWORK) msg = c.getString(R.string.auth_network);
        else if (r.code == CloudAuth.WRONG) msg = c.getString(R.string.err_wrong_login);
        else if (r.detail != null && r.detail.toUpperCase(Locale.US).contains("CANCEL")) msg = c.getString(R.string.google_cancelled);
        else msg = c.getString(R.string.auth_failed, r.detail);
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }
}