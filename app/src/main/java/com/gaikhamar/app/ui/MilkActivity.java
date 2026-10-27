package com.gaikhamar.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.databinding.ActivityMilkBinding;
import com.gaikhamar.app.databinding.DialogAddMilkBinding;
import com.gaikhamar.app.util.InputUtil;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.viewmodel.MilkViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MilkActivity extends BaseActivity {

    private static final String[] SESSIONS = {"MORNING", "EVENING"};

    private ActivityMilkBinding b;
    private MilkViewModel vm;
    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMilkBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = Repository.get(this);
        vm = new ViewModelProvider(this).get(MilkViewModel.class);

        MilkAdapter adapter = new MilkAdapter();
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.btnDate.setText(vm.getDate());
        b.btnDate.setOnClickListener(v -> pickDate());

        vm.records.observe(this, list -> {
            adapter.submitList(list);
            b.tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        vm.total.observe(this, t ->
                b.tvSummary.setText(getString(R.string.total_milk, NumFmt.n(t == null ? 0 : t))));

        b.fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            String s = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            vm.setDate(s);
            b.btnDate.setText(s);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showAddDialog() {
        List<Animal> cows = vm.milkingCows.getValue();
        if (cows == null || cows.isEmpty()) {
            Toast.makeText(this, R.string.no_milking_cows, Toast.LENGTH_LONG).show();
            return;
        }

        DialogAddMilkBinding d = DialogAddMilkBinding.inflate(getLayoutInflater());

        String[] cowLabels = new String[cows.size()];
        for (int i = 0; i < cows.size(); i++) {
            Animal a = cows.get(i);
            boolean hasTag = a.earTag != null && !a.earTag.isEmpty();
            cowLabels[i] = a.farmCattleId + (hasTag ? " (" + a.earTag + ")" : "");
        }
        d.spCow.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, cowLabels));

        String[] sessionLabels = new String[SESSIONS.length];
        for (int i = 0; i < SESSIONS.length; i++) {
            sessionLabels[i] = LabelHelper.milkSession(this, SESSIONS[i]);
        }
        d.spSession.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, sessionLabels));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_milk)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double liters = InputUtil.num(d.etLiters);
            if (liters <= 0) {
                Toast.makeText(this, R.string.err_liters, Toast.LENGTH_SHORT).show();
                return;
            }
            Animal cow = cows.get(d.spCow.getSelectedItemPosition());
            String session = SESSIONS[d.spSession.getSelectedItemPosition()];
            repo.addMilk(cow.id, vm.getDate(), session, liters, ok ->
                    Toast.makeText(this, R.string.milk_saved, Toast.LENGTH_SHORT).show());
            dialog.dismiss();
        });
    }
}