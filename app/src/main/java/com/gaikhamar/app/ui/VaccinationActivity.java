package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Vaccination;
import com.gaikhamar.app.databinding.ActivityListBinding;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.viewmodel.HealthViewModel;

import java.util.ArrayList;
import java.util.List;

public class VaccinationActivity extends BaseActivity {

    private ActivityListBinding b;
    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = Repository.get(this);

        b.tvTitle.setText(R.string.vaccines);
        b.tvEmpty.setText(R.string.no_vaccines);
        b.actions.setVisibility(View.VISIBLE);
        b.btnAction1.setVisibility(View.VISIBLE);
        b.btnAction1.setText(R.string.add_common_vaccines);
        b.btnAction1.setOnClickListener(v -> addCommon());
        b.fab.setOnClickListener(v -> showAddDialog());

        RowAdapter adapter = new RowAdapter(row -> showGivenDialog((Vaccination) row.tag));
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        HealthViewModel vm = new ViewModelProvider(this).get(HealthViewModel.class);
        vm.vaccinations.observe(this, list -> {
            adapter.submitList(toRows(list));
            b.tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private List<Row> toRows(List<Vaccination> list) {
        List<Row> rows = new ArrayList<>();
        if (list == null) return rows;
        String today = DateUtil.today();
        int red = ContextCompat.getColor(this, R.color.red_600);
        int amber = ContextCompat.getColor(this, R.color.amber_700);
        int green = ContextCompat.getColor(this, R.color.green_700);
        for (Vaccination v : list) {
            long days = DateUtil.daysBetween(today, v.nextDueDate);
            int color = days < 0 ? red : days <= 7 ? amber : green;
            int status = days < 0 ? R.string.v_overdue : days <= 7 ? R.string.v_due_soon : R.string.v_ok;
            String last = v.lastDate.isEmpty() ? getString(R.string.v_never) : getString(R.string.v_last, v.lastDate);
            String sub = last + "  |  " + getString(R.string.v_every, String.valueOf(v.intervalDays));
            rows.add(new Row(v.id, v.name, sub, getString(status),
                    getString(R.string.v_next, v.nextDueDate), color, v));
        }
        return rows;
    }

    private void addCommon() {
        String[] names = {getString(R.string.vac_fmd), getString(R.string.vac_anthrax),
                getString(R.string.vac_hs), getString(R.string.vac_bq), getString(R.string.vac_deworm)};
        // Typical intervals. Please confirm them with your local vet.
        int[] days = {180, 365, 365, 365, 90};
        List<Vaccination> list = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Vaccination v = new Vaccination();
            v.name = names[i];
            v.intervalDays = days[i];
            v.nextDueDate = DateUtil.today();
            v.createdAt = System.currentTimeMillis();
            list.add(v);
        }
        repo.addVaccinations(list, null);
    }

    private void showAddDialog() {
        new FormDialog.Builder(this, R.string.add_vaccine)
                .text("name", R.string.vaccine_name)
                .number("days", R.string.interval_days, "180")
                .date("last", R.string.last_given)
                .show(v -> {
                    String name = v.str("name");
                    int days = (int) v.num("days");
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.err_vaccine_name, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (days <= 0) days = 180;
                    Vaccination vac = new Vaccination();
                    vac.name = name;
                    vac.intervalDays = days;
                    vac.lastDate = v.str("last");
                    vac.nextDueDate = DateUtil.plusDays(vac.lastDate, days);
                    vac.createdAt = System.currentTimeMillis();
                    List<Vaccination> one = new ArrayList<>();
                    one.add(vac);
                    repo.addVaccinations(one, null);
                    return true;
                });
    }

    private void showGivenDialog(Vaccination vac) {
        new FormDialog.Builder(this, R.string.mark_given)
                .date("date", R.string.date)
                .number("cost", R.string.cost_optional)
                .show(v -> {
                    repo.markVaccinationGiven(vac.id, v.str("date"), v.num("cost"), ok ->
                            Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show());
                    return true;
                });
    }
}