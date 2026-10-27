package com.gaikhamar.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.entity.BatchSummary;
import com.gaikhamar.app.databinding.ItemBatchBinding;
import com.gaikhamar.app.util.NumFmt;

import java.util.Objects;

public class BatchAdapter extends ListAdapter<BatchSummary, BatchAdapter.VH> {

    private static final DiffUtil.ItemCallback<BatchSummary> DIFF = new DiffUtil.ItemCallback<BatchSummary>() {
        @Override
        public boolean areItemsTheSame(@NonNull BatchSummary o, @NonNull BatchSummary n) {
            return o.id == n.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull BatchSummary o, @NonNull BatchSummary n) {
            return Objects.equals(o.name, n.name)
                    && Objects.equals(o.targetSellDate, n.targetSellDate)
                    && o.animalCount == n.animalCount
                    && o.totalCost == n.totalCost
                    && o.expectedValue == n.expectedValue;
        }
    };

    public BatchAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemBatchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BatchSummary s = getItem(position);
        ItemBatchBinding b = h.b;
        Context c = b.getRoot().getContext();

        b.tvName.setText(s.name);
        String target = s.targetSellDate == null || s.targetSellDate.isEmpty() ? "-" : s.targetSellDate;
        b.tvTarget.setText(c.getString(R.string.batch_target, target));
        b.tvAnimals.setText(c.getString(R.string.batch_animals, String.valueOf(s.animalCount)));
        b.tvCost.setText(c.getString(R.string.batch_cost, NumFmt.n(s.totalCost)));
        b.tvValue.setText(c.getString(R.string.batch_value, NumFmt.n(s.expectedValue)));

        if (s.expectedValue > 0) {
            double profit = s.expectedValue - s.totalCost;
            b.tvProfit.setText(c.getString(R.string.batch_profit, NumFmt.n(profit)));
            b.tvProfit.setTextColor(ContextCompat.getColor(c,
                    profit >= 0 ? R.color.green_700 : R.color.red_600));
            b.tvProfit.setVisibility(View.VISIBLE);
        } else {
            b.tvProfit.setVisibility(View.GONE);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemBatchBinding b;
        VH(ItemBatchBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}