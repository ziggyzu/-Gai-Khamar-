package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.FeedType;
import com.gaikhamar.app.databinding.ActivityFeedStockBinding;
import com.gaikhamar.app.databinding.DialogAddFeedTypeBinding;
import com.gaikhamar.app.databinding.DialogAddStockBinding;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.InputUtil;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.viewmodel.FeedViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedStockActivity extends BaseActivity {

    private ActivityFeedStockBinding b;
    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFeedStockBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = Repository.get(this);
        FeedViewModel vm = new ViewModelProvider(this).get(FeedViewModel.class);

        FeedTypeAdapter adapter = new FeedTypeAdapter(this::showAddStockDialog);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        vm.feedTypes.observe(this, list -> {
            adapter.submitList(list);
            boolean empty = list == null || list.isEmpty();
            b.emptyBox.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        b.fabAdd.setOnClickListener(v -> showAddFeedTypeDialog());
        b.btnCommon.setOnClickListener(v -> addCommonFeeds());
    }

    private void addCommonFeeds() {
        String[] names = {
                getString(R.string.f_green_fodder),
                getString(R.string.f_straw),
                getString(R.string.f_concentrate),
                getString(R.string.f_silage),
                getString(R.string.f_mineral)
        };
        List<FeedType> list = new ArrayList<>();
        for (String n : names) {
            FeedType t = new FeedType();
            t.name = n;
            t.lowStockLevelKg = 50;
            t.createdAt = System.currentTimeMillis();
            list.add(t);
        }
        repo.addFeedTypes(list, null);
    }

    private void showAddFeedTypeDialog() {
        DialogAddFeedTypeBinding d = DialogAddFeedTypeBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_feed_type)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.add, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = InputUtil.text(d.etFeedName);
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.err_feed_name, Toast.LENGTH_SHORT).show();
                return;
            }
            FeedType t = new FeedType();
            t.name = name;
            t.costPerKg = InputUtil.num(d.etCostPerKg);
            t.stockKg = InputUtil.num(d.etOpeningStock);
            t.lowStockLevelKg = InputUtil.num(d.etLowLevel);
            t.createdAt = System.currentTimeMillis();
            repo.addFeedTypes(Collections.singletonList(t), null);
            dialog.dismiss();
        });
    }

    private void showAddStockDialog(FeedType type) {
        DialogAddStockBinding d = DialogAddStockBinding.inflate(getLayoutInflater());
        d.etPrice.setText(NumFmt.plain(type.costPerKg));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.add_stock) + ": " + type.name)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.add, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double qty = InputUtil.num(d.etQty);
            if (qty <= 0) {
                Toast.makeText(this, R.string.err_quantity, Toast.LENGTH_SHORT).show();
                return;
            }
            double price = InputUtil.num(d.etPrice);
            repo.addStock(type.id, qty, price, DateUtil.today(), ok ->
                    Toast.makeText(this, R.string.stock_added, Toast.LENGTH_SHORT).show());
            dialog.dismiss();
        });
    }
}