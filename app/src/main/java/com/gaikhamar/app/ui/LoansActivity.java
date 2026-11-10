package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.ExtraRepository;
import com.gaikhamar.app.data.entity.Loan;
import com.gaikhamar.app.databinding.ActivityListBinding;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.NumFmt;

import java.util.ArrayList;
import java.util.List;

public class LoansActivity extends BaseActivity {

    private ActivityListBinding b;
    private ExtraRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = ExtraRepository.get(this);

        b.tvTitle.setText(R.string.loans);
        b.tvEmpty.setText(R.string.no_loans);
        b.fab.setOnClickListener(v -> showAdd());

        RowAdapter adapter = new RowAdapter(row -> {
            Loan l = (Loan) row.tag;
            if (l.balance > 0) showPay(l);
        });
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        int red = ContextCompat.getColor(this, R.color.red_600);
        int green = ContextCompat.getColor(this, R.color.green_700);
        String today = DateUtil.today();

        repo.loans().observe(this, list -> {
            List<Row> rows = new ArrayList<>();
            if (list != null) {
                for (Loan l : list) {
                    boolean paid = l.balance <= 0;
                    boolean overdue = !paid && !l.nextDueDate.isEmpty() && l.nextDueDate.compareTo(today) < 0;
                    String sub = paid ? getString(R.string.loan_paid)
                            : getString(R.string.loan_installment, NumFmt.n(l.installment))
                            + "  |  " + getString(R.string.loan_next, l.nextDueDate);
                    rows.add(new Row(l.id, l.lender, sub, getString(R.string.fmt_money, NumFmt.n(l.balance)),
                            getString(R.string.loan_balance), paid ? green : overdue ? red : 0, l));
                }
            }
            adapter.submitList(rows);
            b.tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showAdd() {
        new FormDialog.Builder(this, R.string.add_loan)
                .text("lender", R.string.lender)
                .number("amount", R.string.loan_amount)
                .number("inst", R.string.installment)
                .date("due", R.string.first_due)
                .show(v -> {
                    if (v.str("lender").isEmpty()) {
                        Toast.makeText(this, R.string.err_lender, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (v.num("amount") <= 0) {
                        Toast.makeText(this, R.string.err_loan, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Loan l = new Loan();
                    l.lender = v.str("lender");
                    l.principal = v.num("amount");
                    l.balance = l.principal;
                    l.installment = v.num("inst");
                    l.nextDueDate = v.str("due");
                    repo.addLoan(l, ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }

    private void showPay(Loan l) {
        new FormDialog.Builder(this, R.string.pay_installment)
                .date("date", R.string.date)
                .number("amount", R.string.amount, l.installment > 0 ? NumFmt.plain(l.installment) : null)
                .show(v -> {
                    if (v.num("amount") <= 0) {
                        Toast.makeText(this, R.string.err_amount, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    repo.payLoan(l.id, v.num("amount"), v.str("date"),
                            ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }
}