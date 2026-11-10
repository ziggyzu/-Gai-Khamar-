package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.ExtraRepository;
import com.gaikhamar.app.data.entity.Worker;
import com.gaikhamar.app.data.entity.WorkerDays;
import com.gaikhamar.app.databinding.ActivityListBinding;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkersActivity extends BaseActivity {

    private ActivityListBinding b;
    private ExtraRepository repo;
    private SessionManager session;
    private RowAdapter adapter;

    private List<Worker> workers = new ArrayList<>();
    private final Set<Long> presentToday = new HashSet<>();
    private final Map<Long, Integer> daysThisMonth = new HashMap<>();
    private final String today = DateUtil.today();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = ExtraRepository.get(this);
        session = new SessionManager(this);

        b.tvTitle.setText(R.string.workers);
        b.tvEmpty.setText(R.string.no_workers);
        b.fab.setOnClickListener(v -> showAdd());

        adapter = new RowAdapter(row -> showActions((Worker) row.tag));
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        String month = DateUtil.monthKey(0);
        repo.workers().observe(this, l -> { workers = l == null ? new ArrayList<>() : l; render(); });
        repo.presentOn(today).observe(this, l -> {
            presentToday.clear();
            if (l != null) presentToday.addAll(l);
            render();
        });
        repo.workerDays(month + "-01", month + "-31").observe(this, l -> {
            daysThisMonth.clear();
            if (l != null) for (WorkerDays d : l) daysThisMonth.put(d.workerId, d.days);
            render();
        });
    }

    private String roleLabel(String code) {
        String[] codes = getResources().getStringArray(R.array.staff_role_codes);
        String[] labels = getResources().getStringArray(R.array.staff_role_labels);
        for (int i = 0; i < codes.length && i < labels.length; i++) {
            if (codes[i].equals(code)) return labels[i];
        }
        return code;
    }

    private void render() {
        int green = ContextCompat.getColor(this, R.color.green_700);
        int red = ContextCompat.getColor(this, R.color.red_600);
        List<Row> rows = new ArrayList<>();
        for (Worker w : workers) {
            boolean here = presentToday.contains(w.id);
            Integer d = daysThisMonth.get(w.id);
            String sub = roleLabel(w.role) + "  |  "
                    + getString(R.string.wage_line, w.phone == null ? "" : w.phone, NumFmt.n(w.dailyWage));
            rows.add(new Row(w.id, w.name, sub, getString(here ? R.string.present : R.string.absent),
                    getString(R.string.days_month, String.valueOf(d == null ? 0 : d)), here ? green : red, w));
        }
        adapter.submitList(rows);
        b.tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showActions(Worker w) {
        String[] items = {getString(R.string.mark_present), getString(R.string.mark_absent),
                getString(R.string.pay_wages)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(w.name)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) repo.setAttendance(w.id, today, true, null);
                    else if (which == 1) repo.setAttendance(w.id, today, false, null);
                    else showPayWages(w);
                })
                .show();
    }

    private void showPayWages(Worker w) {
        Integer d = daysThisMonth.get(w.id);
        double owed = (d == null ? 0 : d) * w.dailyWage;
        new FormDialog.Builder(this, R.string.pay_wages)
                .date("date", R.string.date)
                .number("amount", R.string.amount, owed > 0 ? NumFmt.plain(owed) : null)
                .show(v -> {
                    if (v.num("amount") <= 0) {
                        Toast.makeText(this, R.string.err_amount, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    repo.payWages(w.name, v.num("amount"), v.str("date"),
                            ok -> Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }

    private void showAdd() {
        String[] codes = getResources().getStringArray(R.array.staff_role_codes);
        String[] labels = getResources().getStringArray(R.array.staff_role_labels);
        new FormDialog.Builder(this, R.string.add_worker)
                .text("name", R.string.full_name)
                .phone("phone", R.string.phone, "")
                .choice("role", R.string.role, labels)
                .number("wage", R.string.wage_per_day)
                .password("pw", R.string.worker_password)
                .show(v -> {
                    if (v.str("name").isEmpty()) {
                        Toast.makeText(this, R.string.err_worker, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    String pw = v.str("pw");
                    if (!pw.isEmpty() && (pw.length() < 6 || v.str("phone").length() < 10)) {
                        Toast.makeText(this, R.string.err_login_password, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Worker w = new Worker();
                    w.name = v.str("name");
                    w.phone = v.str("phone");
                    w.role = codes[v.choice("role")];
                    w.dailyWage = v.num("wage");
                    repo.addWorker(w, pw, session.getFarmCode(), session.getFarmName(), result ->
                            Toast.makeText(this, result == 1 ? R.string.err_phone_exists : R.string.saved_ok,
                                    Toast.LENGTH_SHORT).show());
                    return true;
                });
    }
}