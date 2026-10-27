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
import com.gaikhamar.app.data.entity.FeedType;
import com.gaikhamar.app.databinding.ActivityFeedLogBinding;
import com.gaikhamar.app.databinding.DialogLogFeedBinding;
import com.gaikhamar.app.util.InputUtil;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.viewmodel.FeedViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FeedLogActivity extends BaseActivity {

    private static final String[] GROUP_CODES = {"BEEF", "MILKING", "DRY", "CALF"};

    private ActivityFeedLogBinding b;
    private FeedViewModel vm;
    private Repository repo;
    private double sumKg = 0;
    private double sumCost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFeedLogBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = Repository.get(this);
        vm = new ViewModelProvider(this).get(FeedViewModel.class);

        FeedLogAdapter adapter = new FeedLogAdapter();
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.btnDate.setText(vm.getDate());
        b.btnDate.setOnClickListener(v -> pickDate());

        vm.logs.observe(this, list -> {
            adapter.submitList(list);
            b.tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        vm.totalKg.observe(this, kg -> {
            sumKg = kg == null ? 0 : kg;
            updateSummary();
        });
        vm.totalCost.observe(this, cost -> {
            sumCost = cost == null ? 0 : cost;
            updateSummary();
        });

        b.fabLog.setOnClickListener(v -> showLogDialog());
    }

    private void updateSummary() {
        b.tvSummary.setText(getString(R.string.total_fed, NumFmt.n(sumKg))
                + "     " + getString(R.string.fmt_cost, NumFmt.n(sumCost)));
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            String s = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            vm.setDate(s);
            b.btnDate.setText(s);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showLogDialog() {
        List<FeedType> types = vm.feedTypes.getValue();
        if (types == null || types.isEmpty()) {
            Toast.makeText(this, R.string.err_no_feed_type, Toast.LENGTH_SHORT).show();
            return;
        }

        DialogLogFeedBinding d = DialogLogFeedBinding.inflate(getLayoutInflater());

        String[] typeLabels = new String[types.size()];
        for (int i = 0; i < types.size(); i++) {
            FeedType t = types.get(i);
            typeLabels[i] = t.name + " (" + getString(R.string.fmt_kg, NumFmt.n(t.stockKg)) + ")";
        }
        d.spFeedType.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, typeLabels));

        String[] groupLabels = new String[GROUP_CODES.length];
        for (int i = 0; i < GROUP_CODES.length; i++) {
            groupLabels[i] = LabelHelper.feedGroup(this, GROUP_CODES[i]);
        }
        d.spGroup.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, groupLabels));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.log_feeding)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double qty = InputUtil.num(d.etQty);
            if (qty <= 0) {
                Toast.makeText(this, R.string.err_quantity, Toast.LENGTH_SHORT).show();
                return;
            }
            FeedType t = types.get(d.spFeedType.getSelectedItemPosition());
            String group = GROUP_CODES[d.spGroup.getSelectedItemPosition()];

            repo.logFeed(t.id, group, qty, vm.getDate(), result -> {
                if (result == 0) {
                    Toast.makeText(this, R.string.feed_logged, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, R.string.err_not_enough_stock, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}