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
import com.gaikhamar.app.data.entity.FeedType;
import com.gaikhamar.app.databinding.ItemFeedTypeBinding;
import com.gaikhamar.app.util.NumFmt;

public class FeedTypeAdapter extends ListAdapter<FeedType, FeedTypeAdapter.VH> {

    public interface OnClick {
        void onClick(FeedType type);
    }

    private static final DiffUtil.ItemCallback<FeedType> DIFF = new DiffUtil.ItemCallback<FeedType>() {
        @Override
        public boolean areItemsTheSame(@NonNull FeedType o, @NonNull FeedType n) {
            return o.id == n.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull FeedType o, @NonNull FeedType n) {
            return o.name.equals(n.name)
                    && o.stockKg == n.stockKg
                    && o.costPerKg == n.costPerKg
                    && o.lowStockLevelKg == n.lowStockLevelKg;
        }
    };

    private final OnClick listener;

    public FeedTypeAdapter(OnClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemFeedTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FeedType t = getItem(position);
        ItemFeedTypeBinding b = h.b;
        Context c = b.getRoot().getContext();

        boolean low = t.stockKg <= t.lowStockLevelKg;
        b.tvName.setText(t.name);
        b.tvStock.setText(c.getString(R.string.fmt_stock, NumFmt.n(t.stockKg)));
        b.tvStock.setTextColor(ContextCompat.getColor(c, low ? R.color.red_600 : R.color.black));
        b.tvCost.setText(c.getString(R.string.fmt_per_kg, NumFmt.n(t.costPerKg)));
        b.tvAlert.setText(c.getString(R.string.fmt_alert_level, NumFmt.n(t.lowStockLevelKg)));
        b.tvLow.setVisibility(low ? View.VISIBLE : View.GONE);
        b.getRoot().setOnClickListener(v -> listener.onClick(t));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemFeedTypeBinding b;
        VH(ItemFeedTypeBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}