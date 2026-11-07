package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.ProfitReport;
import com.gaikhamar.app.data.entity.ProfitRow;
import com.gaikhamar.app.databinding.ActivityProfitBinding;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.NumFmt;

import java.util.ArrayList;
import java.util.List;

public class ProfitActivity extends BaseActivity {

    private ActivityProfitBinding b;
    private Repository repo;
    private RowAdapter adapter;
    private ProfitReport report;
    private int mode = 0;   // 0 animals, 1 batches, 2 groups

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProfitBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        repo = Repository.get(this);

        adapter = new RowAdapter(null);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.chips.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            mode = id == R.id.chipBatch ? 1 : id == R.id.chipGroup ? 2 : 0;
            render();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        repo.loadProfit(r -> {
            report = r;
            render();
        });
    }

    private void render() {
        if (report == null) return;
        List<ProfitRow> src = mode == 0 ? report.animals : mode == 1 ? report.batches : report.categories;
        int red = ContextCompat.getColor(this, R.color.red_600);
        int green = ContextCompat.getColor(this, R.color.green_700);

        List<Row> rows = new ArrayList<>();
        for (ProfitRow p : src) {
            String title;
            if (mode == 0) title = p.title;
            else if (mode == 1) title = p.title == null || p.title.isEmpty() ? getString(R.string.no_batch) : p.title;
            else title = LabelHelper.category(this, p.category);

            String sub = getString(R.string.p_income_cost, NumFmt.n(p.income), NumFmt.n(p.cost));
            if (mode == 0) sub = LabelHelper.category(this, p.category) + "  |  " + sub;

            double profit = p.profit();
            rows.add(new Row(p.id, title, sub, getString(R.string.fmt_money, NumFmt.n(profit)),
                    getString(profit < 0 ? R.string.p_loss : R.string.profit), profit < 0 ? red : green, p));
        }
        adapter.submitList(rows);
        b.tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }
}