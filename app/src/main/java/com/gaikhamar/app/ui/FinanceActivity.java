package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.CategoryTotal;
import com.gaikhamar.app.data.entity.FinanceRecord;
import com.gaikhamar.app.databinding.ActivityFinanceBinding;
import com.gaikhamar.app.util.CsvExport;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.util.PdfReport;
import com.gaikhamar.app.util.SessionManager;
import com.gaikhamar.app.util.ShareHelper;
import com.gaikhamar.app.viewmodel.FinanceViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FinanceActivity extends BaseActivity {

    private ActivityFinanceBinding b;
    private FinanceViewModel vm;
    private Repository repo;
    private SessionManager session;
    private double inc = 0, exp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFinanceBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = Repository.get(this);
        session = new SessionManager(this);
        vm = new ViewModelProvider(this).get(FinanceViewModel.class);

        RowAdapter adapter = new RowAdapter(null);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        vm.monthLive.observe(this, m -> b.tvMonth.setText(m));
        vm.income.observe(this, v -> { inc = v == null ? 0 : v; updateSummary(); });
        vm.expense.observe(this, v -> { exp = v == null ? 0 : v; updateSummary(); });
        vm.records.observe(this, list -> {
            int green = ContextCompat.getColor(this, R.color.green_700);
            int red = ContextCompat.getColor(this, R.color.red_600);
            List<Row> rows = new ArrayList<>();
            if (list != null) {
                for (FinanceRecord r : list) {
                    boolean in = "INCOME".equals(r.type);
                    String sub = r.date + (r.note == null || r.note.isEmpty() ? "" : "  |  " + r.note);
                    rows.add(new Row(r.id, LabelHelper.finCategory(this, r.category), sub,
                            (in ? "+" : "-") + getString(R.string.fmt_money, NumFmt.n(r.amount)),
                            "", in ? green : red, r));
                }
            }
            adapter.submitList(rows);
            b.tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
            loadChart();
        });

        b.btnPrev.setOnClickListener(v -> vm.shiftMonth(-1));
        b.btnNext.setOnClickListener(v -> vm.shiftMonth(1));
        b.btnAddIncome.setOnClickListener(v -> showAdd("INCOME"));
        b.btnAddExpense.setOnClickListener(v -> showAdd("EXPENSE"));
        b.btnPdf.setOnClickListener(v -> exportPdf());
        b.btnCsv.setOnClickListener(v -> exportCsv());
        b.btnProfit.setOnClickListener(v -> startActivity(new Intent(this, ProfitActivity.class)));
    }

    private void updateSummary() {
        b.tvIncome.setText(getString(R.string.s_income, NumFmt.n(inc)));
        b.tvExpense.setText(getString(R.string.s_expense, NumFmt.n(exp)));
        double p = inc - exp;
        b.tvProfit.setText(getString(R.string.s_profit, NumFmt.n(p)));
        b.tvProfit.setTextColor(ContextCompat.getColor(this, p >= 0 ? R.color.green_700 : R.color.red_600));
    }

    private void loadChart() {
        String[] keys = DateUtil.lastMonthKeys(6);
        repo.monthlySeries(keys, data -> {
            String[] labels = new String[keys.length];
            for (int i = 0; i < keys.length; i++) labels[i] = keys[i].substring(5);
            b.chart.setData(labels, data[0], data[1]);
        });
    }

    private void showAdd(String type) {
        boolean income = "INCOME".equals(type);
        String[] codes = getResources().getStringArray(income ? R.array.income_codes : R.array.expense_codes);
        String[] labels = getResources().getStringArray(income ? R.array.income_labels : R.array.expense_labels);
        new FormDialog.Builder(this, income ? R.string.add_income : R.string.add_expense)
                .date("date", R.string.date)
                .choice("cat", R.string.category, labels)
                .number("amount", R.string.amount)
                .text("note", R.string.note)
                .show(v -> {
                    double amount = v.num("amount");
                    if (amount <= 0) {
                        Toast.makeText(this, R.string.err_amount, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    repo.addFinance(type, codes[v.choice("cat")], amount, v.str("date"), v.str("note"), 0,
                            ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }

    private void exportPdf() {
        final String m = vm.getMonth();
        final String from = m + "-01", to = m + "-31";
        new Thread(() -> {
            try {
                File dir = new File(getCacheDir(), "reports");
                dir.mkdirs();
                File f = new File(dir, "report-" + m + ".pdf");
                double income = repo.sumSync("INCOME", from, to);
                double expense = repo.sumSync("EXPENSE", from, to);
                List<CategoryTotal> ic = repo.categoryTotalsSync("INCOME", from, to);
                List<CategoryTotal> ec = repo.categoryTotalsSync("EXPENSE", from, to);
                PdfReport.create(this, f, session.getFarmName(), m, income, expense, ic, ec);
                runOnUiThread(() -> ShareHelper.shareFile(this, f, "application/pdf", getString(R.string.share_report)));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void exportCsv() {
        final String m = vm.getMonth();
        new Thread(() -> {
            try {
                File dir = new File(getCacheDir(), "reports");
                dir.mkdirs();
                File f = new File(dir, "finance-" + m + ".csv");
                CsvExport.write(f, repo.financeBetweenSync(m + "-01", m + "-31"));
                runOnUiThread(() -> ShareHelper.shareFile(this, f, "text/csv", getString(R.string.share_report)));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}