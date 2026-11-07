package com.gaikhamar.app.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.databinding.ActivityListBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactPickerActivity extends BaseActivity {

    private ActivityListBinding b;
    private RowAdapter adapter;
    private final List<Row> all = new ArrayList<>();

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    load();
                } else {
                    showMessage(R.string.contacts_denied);
                    b.btnAction1.setText(R.string.open_settings);
                    b.btnAction1.setOnClickListener(v -> startActivity(new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName()))));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.tvTitle.setText(R.string.pick_contact);
        b.fab.setVisibility(View.GONE);
        b.tilSearch.setVisibility(View.VISIBLE);
        b.tilSearch.setHint(getString(R.string.search_contacts));

        adapter = new RowAdapter(row -> {
            String[] data = (String[]) row.tag;
            Intent result = new Intent();
            result.putExtra("name", data[0]);
            result.putExtra("phone", data[1]);
            setResult(RESULT_OK, result);
            finish();
        });
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int c, int d) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            load();
        } else {
            // Explain first, then ask when the user taps the button
            showMessage(R.string.contacts_permission_needed);
            b.actions.setVisibility(View.VISIBLE);
            b.btnAction1.setVisibility(View.VISIBLE);
            b.btnAction1.setText(R.string.allow_access);
            b.btnAction1.setOnClickListener(v -> permissionLauncher.launch(Manifest.permission.READ_CONTACTS));
        }
    }

    private void showMessage(int res) {
        b.tvEmpty.setText(res);
        b.tvEmpty.setVisibility(View.VISIBLE);
        b.actions.setVisibility(View.VISIBLE);
        b.btnAction1.setVisibility(View.VISIBLE);
    }

    /** Reads the phone's Contacts Content Provider on a background thread. */
    private void load() {
        b.actions.setVisibility(View.GONE);
        b.tvEmpty.setVisibility(View.GONE);
        new Thread(() -> {
            List<Row> rows = new ArrayList<>();
            try (Cursor c = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")) {
                if (c != null) {
                    long id = 0;
                    while (c.moveToNext()) {
                        String name = c.getString(0);
                        String number = c.getString(1);
                        if (name == null || number == null) continue;
                        rows.add(new Row(++id, name, number, "", "", 0, new String[]{name, number}));
                    }
                }
            }
            runOnUiThread(() -> {
                all.clear();
                all.addAll(rows);
                adapter.submitList(new ArrayList<>(all));
                if (all.isEmpty()) {
                    b.tvEmpty.setText(R.string.no_contacts);
                    b.tvEmpty.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void filter(String q) {
        String s = q.toLowerCase(Locale.ROOT);
        List<Row> out = new ArrayList<>();
        for (Row r : all) {
            if (r.title.toLowerCase(Locale.ROOT).contains(s) || r.subtitle.contains(s)) out.add(r);
        }
        adapter.submitList(out);
    }
}