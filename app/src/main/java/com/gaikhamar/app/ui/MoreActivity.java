package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.gaikhamar.app.R;
import com.gaikhamar.app.cloud.CloudAuth;
import com.gaikhamar.app.cloud.CloudSync;
import com.gaikhamar.app.data.ExtraRepository;
import com.gaikhamar.app.databinding.ActivityMoreBinding;
import com.gaikhamar.app.util.Access;
import com.gaikhamar.app.util.AppPrefs;
import com.gaikhamar.app.util.BiometricHelper;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.LoginIds;
import com.gaikhamar.app.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MoreActivity extends BaseActivity {

    private ActivityMoreBinding b;
    private AppPrefs prefs;
    private boolean admin;
    private MaterialButton bioButton, syncButton, restoreButton, connectButton;

    private final ActivityResultLauncher<ScanOptions> scanner = registerForActivityResult(
            new ScanContract(), result -> {
                if (result.getContents() != null) openByCode(result.getContents().trim());
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMoreBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        prefs = new AppPrefs(this);

        String role = new SessionManager(this).getRole();
        admin = Access.isAdmin(role);
        boolean money = Access.canSeeMoney(role);

        addButton(R.string.scan_qr, true, this::scan);
        addButton(R.string.market_prices, money, () -> openBoard("prices"));
        addButton(R.string.farm_tasks, true, () -> openBoard("tasks"));
        addButton(R.string.loans, money, () -> startActivity(new Intent(this, LoansActivity.class)));
        addButton(R.string.workers, admin, () -> startActivity(new Intent(this, WorkersActivity.class)));
        connectButton = addButton(R.string.connect_cloud, admin, this::connectCloud);
        syncButton = addButton(R.string.cloud_sync, admin, () -> runCloud(false));
        restoreButton = addButton(R.string.cloud_restore, admin, this::confirmRestore);
        bioButton = addButton(R.string.bio_toggle_off, true, this::toggleBiometric);
        updateBioButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private MaterialButton addButton(int textRes, boolean visible, Runnable action) {
        MaterialButton btn = new MaterialButton(this);
        btn.setText(textRes);
        btn.setOnClickListener(v -> action.run());
        btn.setVisibility(visible ? View.VISIBLE : View.GONE);
        float d = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (52 * d));
        lp.topMargin = (int) (8 * d);
        b.list.addView(btn, lp);
        return btn;
    }

    private void toast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    // ---------------------------------------------------------------- QR

    private void scan() {
        ScanOptions o = new ScanOptions();
        o.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        o.setPrompt(getString(R.string.scan_prompt));
        o.setBeepEnabled(false);
        o.setOrientationLocked(false);
        scanner.launch(o);
    }

    private void openByCode(String code) {
        ExtraRepository.get(this).findAnimalIdByCode(code, id -> {
            if (id == null) {
                toast(R.string.scan_not_found);
            } else {
                Intent i = new Intent(this, AnimalDetailActivity.class);
                i.putExtra(AnimalDetailActivity.EXTRA_ID, (long) id);
                startActivity(i);
            }
        });
    }

    private void openBoard(String kind) {
        Intent i = new Intent(this, BoardActivity.class);
        i.putExtra(BoardActivity.EXTRA_KIND, kind);
        startActivity(i);
    }

    // ------------------------------------------------------------- cloud

    private void updateStatus() {
        String email = CloudAuth.email();
        boolean connected = CloudAuth.uid() != null;
        b.tvCloud.setText(connected
                ? getString(R.string.cloud_connected_as, email == null ? "" : email)
                : getString(R.string.cloud_not_connected));
        connectButton.setVisibility(admin && !connected ? View.VISIBLE : View.GONE);

        long t = prefs.lastSync();
        b.tvSync.setText(t == 0 ? getString(R.string.sync_never)
                : getString(R.string.sync_last, DateUtils.formatDateTime(this, t,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL)));
    }

    /** Connects this phone to a cloud account without logging out. */
    private void connectCloud() {
        String[] items = {getString(R.string.connect_email), getString(R.string.connect_google)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.connect_title)
                .setItems(items, (d, which) -> {
                    if (which == 0) connectWithEmail();
                    else CloudAuth.signInGoogle(this, this::afterConnect);
                })
                .show();
    }

    private void connectWithEmail() {
        new FormDialog.Builder(this, R.string.connect_email)
                .text("email", R.string.email, "")
                .password("pw", R.string.password)
                .show(v -> {
                    String email = LoginIds.normalize(v.str("email"));
                    if (!LoginIds.isEmail(email) || v.str("pw").length() < 6) {
                        toast(R.string.err_email);
                        return false;
                    }
                    CloudAuth.signInOrCreate(email, v.str("pw"), this::afterConnect);
                    return true;
                });
    }

    private void afterConnect(CloudAuth.Result r) {
        if (r.ok()) {
            toast(R.string.connected_ok);
            updateStatus();
        } else {
            AuthHelper.toastError(this, r);
        }
    }

    private void confirmRestore() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.restore_title)
                .setMessage(R.string.restore_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.restore, (d, w) -> runCloud(true))
                .show();
    }

    private void runCloud(boolean restore) {
        if (!CloudSync.isOnline(this)) {
            toast(R.string.sync_offline);
            return;
        }
        syncButton.setEnabled(false);
        restoreButton.setEnabled(false);
        toast(R.string.sync_busy);
        CloudSync.Listener l = (ok, msg) -> {
            syncButton.setEnabled(true);
            restoreButton.setEnabled(true);
            toast(msg);
            updateStatus();
        };
        if (restore) CloudSync.restore(this, l); else CloudSync.push(this, l);
    }

    // ------------------------------------------------------- fingerprint

    private void updateBioButton() {
        bioButton.setText(prefs.biometric() ? R.string.bio_toggle_on : R.string.bio_toggle_off);
    }

    private void toggleBiometric() {
        if (prefs.biometric()) {
            prefs.setBiometric(false);
            updateBioButton();
            return;
        }
        if (!BiometricHelper.available(this)) {
            toast(R.string.bio_unavailable);
            return;
        }
        BiometricHelper.prompt(this, R.string.bio_title, new BiometricHelper.Result() {
            @Override public void onSuccess() { prefs.setBiometric(true); updateBioButton(); }
            @Override public void onFail() { toast(R.string.bio_failed); }
        });
    }
}