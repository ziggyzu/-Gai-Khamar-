package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Customer;
import com.gaikhamar.app.data.entity.CustomerBalance;
import com.gaikhamar.app.databinding.ActivityListBinding;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.NumFmt;

import java.util.ArrayList;
import java.util.List;

public class CustomersActivity extends BaseActivity {

    private ActivityListBinding b;
    private Repository repo;

    private final ActivityResultLauncher<Intent> picker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == RESULT_OK && data != null) {
                    showAdd(data.getStringExtra("name"), data.getStringExtra("phone"));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = Repository.get(this);

        b.tvTitle.setText(R.string.customers);
        b.tvEmpty.setText(R.string.no_customers);
        b.actions.setVisibility(View.VISIBLE);
        b.btnAction1.setVisibility(View.VISIBLE);
        b.btnAction1.setText(R.string.from_contacts);
        b.btnAction1.setOnClickListener(v -> picker.launch(new Intent(this, ContactPickerActivity.class)));
        b.fab.setOnClickListener(v -> showAdd("", ""));

        RowAdapter adapter = new RowAdapter(row -> {
            Intent i = new Intent(this, LedgerActivity.class);
            i.putExtra(LedgerActivity.EXTRA_ID, row.id);
            startActivity(i);
        });
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        int red = ContextCompat.getColor(this, R.color.red_600);
        int green = ContextCompat.getColor(this, R.color.green_700);
        repo.customerBalances().observe(this, list -> {
            List<Row> rows = new ArrayList<>();
            if (list != null) {
                for (CustomerBalance c : list) {
                    String sub = (c.phone == null ? "" : c.phone + "  |  ")
                            + getString(R.string.fmt_rate, NumFmt.n(c.ratePerLiter));
                    rows.add(new Row(c.id, c.name, sub, getString(R.string.fmt_money, NumFmt.n(c.balance)),
                            getString(R.string.due), c.balance > 0 ? red : green, c));
                }
            }
            adapter.submitList(rows);
            b.tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showAdd(String name, String phone) {
        new FormDialog.Builder(this, R.string.add_customer)
                .text("name", R.string.full_name, name == null ? "" : name)
                .phone("phone", R.string.phone, phone == null ? "" : phone)
                .number("rate", R.string.rate_per_liter)
                .show(v -> {
                    if (v.str("name").isEmpty()) {
                        Toast.makeText(this, R.string.err_name_phone, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Customer c = new Customer();
                    c.name = v.str("name");
                    c.phone = v.str("phone");
                    c.ratePerLiter = v.num("rate");
                    repo.addCustomer(c, ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }
}