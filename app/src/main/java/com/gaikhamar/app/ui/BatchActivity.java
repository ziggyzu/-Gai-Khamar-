package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Batch;
import com.gaikhamar.app.databinding.ActivityBatchBinding;
import com.gaikhamar.app.databinding.DialogAddBatchBinding;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.InputUtil;
import com.gaikhamar.app.viewmodel.BatchViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BatchActivity extends BaseActivity {

    private ActivityBatchBinding b;
    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityBatchBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = Repository.get(this);
        BatchViewModel vm = new ViewModelProvider(this).get(BatchViewModel.class);

        BatchAdapter adapter = new BatchAdapter();
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        vm.summaries.observe(this, list -> {
            adapter.submitList(list);
            b.tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        b.fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        DialogAddBatchBinding d = DialogAddBatchBinding.inflate(getLayoutInflater());
        InputUtil.dateField(this, d.etTargetDate);
        d.etTargetDate.setText(DateUtil.plusDays(DateUtil.today(), 90)); // default: 3 months

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_batch)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = InputUtil.text(d.etBatchName);
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.err_batch_name, Toast.LENGTH_SHORT).show();
                return;
            }
            Batch batch = new Batch();
            batch.name = name;
            batch.startDate = DateUtil.today();
            batch.targetSellDate = InputUtil.text(d.etTargetDate);
            repo.addBatch(batch, null);
            dialog.dismiss();
        });
    }
}