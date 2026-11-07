package com.gaikhamar.app.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.FarmContact;
import com.gaikhamar.app.databinding.ActivityListBinding;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.viewmodel.HealthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends BaseActivity {

    private ActivityListBinding b;
    private Repository repo;

    private final ActivityResultLauncher<Intent> picker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == RESULT_OK && data != null) {
                    showAddDialog(data.getStringExtra("name"), data.getStringExtra("phone"));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = Repository.get(this);

        b.tvTitle.setText(R.string.contacts);
        b.tvEmpty.setText(R.string.no_saved_contacts);
        b.actions.setVisibility(View.VISIBLE);
        b.btnAction1.setVisibility(View.VISIBLE);
        b.btnAction1.setText(R.string.from_contacts);
        b.btnAction1.setOnClickListener(v -> picker.launch(new Intent(this, ContactPickerActivity.class)));
        b.fab.setOnClickListener(v -> showAddDialog("", ""));

        RowAdapter adapter = new RowAdapter(row -> showActions((FarmContact) row.tag));
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        HealthViewModel vm = new ViewModelProvider(this).get(HealthViewModel.class);
        vm.contacts.observe(this, list -> {
            List<Row> rows = new ArrayList<>();
            if (list != null) {
                for (FarmContact c : list) {
                    rows.add(new Row(c.id, c.name, c.phone, LabelHelper.role(this, c.role), "", 0, c));
                }
            }
            adapter.submitList(rows);
            b.tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showAddDialog(String name, String phone) {
        String[] codes = getResources().getStringArray(R.array.role_codes);
        String[] labels = getResources().getStringArray(R.array.role_labels);
        new FormDialog.Builder(this, R.string.add_contact)
                .text("name", R.string.full_name, name == null ? "" : name)
                .phone("phone", R.string.phone, phone == null ? "" : phone)
                .choice("role", R.string.role, labels)
                .show(v -> {
                    if (v.str("name").isEmpty() || v.str("phone").isEmpty()) {
                        Toast.makeText(this, R.string.err_name_phone, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    FarmContact c = new FarmContact();
                    c.name = v.str("name");
                    c.phone = v.str("phone");
                    c.role = codes[v.choice("role")];
                    repo.addContact(c, ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }

    /** Call and message use implicit intents, so the phone's own apps do the work. */
    private void showActions(FarmContact c) {
        String[] items = {getString(R.string.call), getString(R.string.send_sms), getString(R.string.delete)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(c.name)
                .setItems(items, (dialog, which) -> {
                    try {
                        if (which == 0) {
                            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.phone)));
                        } else if (which == 1) {
                            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + c.phone)));
                        } else {
                            repo.deleteContact(c, null);
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}