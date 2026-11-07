package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Customer;
import com.gaikhamar.app.data.entity.LedgerEntry;
import com.gaikhamar.app.databinding.ActivityListBinding;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.util.ShareHelper;

import java.util.ArrayList;
import java.util.List;

public class LedgerActivity extends BaseActivity {

    public static final String EXTRA_ID = "customer_id";

    private ActivityListBinding b;
    private Repository repo;
    private long customerId;
    private Customer customer;
    private double balance = 0;
    private List<LedgerEntry> entries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = Repository.get(this);
        customerId = getIntent().getLongExtra(EXTRA_ID, -1);

        b.fab.setVisibility(View.GONE);
        b.tvSubtitle.setVisibility(View.VISIBLE);
        b.tvEmpty.setText(R.string.no_entries);
        b.actions.setVisibility(View.VISIBLE);
        b.btnAction1.setVisibility(View.VISIBLE);
        b.btnAction2.setVisibility(View.VISIBLE);
        b.btnAction3.setVisibility(View.VISIBLE);
        b.btnAction1.setText(R.string.ledger_delivery);
        b.btnAction2.setText(R.string.ledger_payment);
        b.btnAction3.setText(R.string.ledger_share);
        b.btnAction1.setOnClickListener(v -> showDelivery());
        b.btnAction2.setOnClickListener(v -> showPayment());
        b.btnAction3.setOnClickListener(v -> shareBill());

        RowAdapter adapter = new RowAdapter(null);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        int red = ContextCompat.getColor(this, R.color.red_600);
        int green = ContextCompat.getColor(this, R.color.green_700);

        repo.customer(customerId).observe(this, c -> {
            customer = c;
            if (c != null) b.tvTitle.setText(c.name);
        });
        repo.customerBalance(customerId).observe(this, v -> {
            balance = v == null ? 0 : v;
            b.tvSubtitle.setText(getString(R.string.balance_due, NumFmt.n(balance)));
        });
        repo.ledger(customerId).observe(this, list -> {
            entries = list == null ? new ArrayList<>() : list;
            List<Row> rows = new ArrayList<>();
            for (LedgerEntry e : entries) {
                boolean pay = "PAYMENT".equals(e.type);
                String sub = pay ? "" : getString(R.string.fmt_liters, NumFmt.n(e.liters));
                rows.add(new Row(e.id, e.date, sub, getString(R.string.fmt_money, NumFmt.n(e.amount)),
                        getString(pay ? R.string.payment : R.string.delivery), pay ? green : red, e));
            }
            adapter.submitList(rows);
            b.tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showDelivery() {
        String rate = customer != null && customer.ratePerLiter > 0 ? NumFmt.plain(customer.ratePerLiter) : null;
        new FormDialog.Builder(this, R.string.ledger_delivery)
                .date("date", R.string.date)
                .number("liters", R.string.liters)
                .number("rate", R.string.rate_per_liter, rate)
                .show(v -> {
                    if (v.num("liters") <= 0) {
                        Toast.makeText(this, R.string.err_liters, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    repo.addDelivery(customerId, v.str("date"), v.num("liters"), v.num("rate"),
                            ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }

    private void showPayment() {
        new FormDialog.Builder(this, R.string.ledger_payment)
                .date("date", R.string.date)
                .number("amount", R.string.amount, balance > 0 ? NumFmt.plain(balance) : null)
                .show(v -> {
                    if (v.num("amount") <= 0) {
                        Toast.makeText(this, R.string.err_amount, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    repo.receivePayment(customerId, v.str("date"), v.num("amount"),
                            ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }

    /** Implicit intent: any messaging app can receive the bill text. */
    private void shareBill() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.bill_title, customer == null ? "" : customer.name)).append("\n");
        int start = Math.min(entries.size(), 20);
        for (int i = start - 1; i >= 0; i--) {   // entries are newest first, so go backwards
            LedgerEntry e = entries.get(i);
            if ("PAYMENT".equals(e.type)) {
                sb.append(getString(R.string.bill_payment, e.date, NumFmt.n(e.amount)));
            } else {
                sb.append(getString(R.string.bill_delivery, e.date, NumFmt.n(e.liters), NumFmt.n(e.amount)));
            }
            sb.append("\n");
        }
        sb.append(getString(R.string.balance_due, NumFmt.n(balance)));
        ShareHelper.shareText(this, sb.toString(), getString(R.string.ledger_share));
    }
}