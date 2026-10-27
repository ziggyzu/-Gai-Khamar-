package com.gaikhamar.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.entity.FeedLog;
import com.gaikhamar.app.databinding.ItemFeedLogBinding;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.NumFmt;

public class FeedLogAdapter extends ListAdapter<FeedLog, FeedLogAdapter.VH> {

    private static final DiffUtil.ItemCallback<FeedLog> DIFF = new DiffUtil.ItemCallback<FeedLog>() {
        @Override
        public boolean areItemsTheSame(@NonNull FeedLog o, @NonNull FeedLog n) {
            return o.id == n.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull FeedLog o, @NonNull FeedLog n) {
            return o.quantityKg == n.quantityKg && o.totalCost == n.totalCost
                    && o.groupCode.equals(n.groupCode) && o.feedName.equals(n.feedName);
        }
    };

    public FeedLogAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemFeedLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FeedLog l = getItem(position);
        ItemFeedLogBinding b = h.b;
        Context c = b.getRoot().getContext();
        b.tvFeed.setText(l.feedName);
        b.tvGroup.setText(LabelHelper.feedGroup(c, l.groupCode));
        b.tvQty.setText(c.getString(R.string.fmt_kg, NumFmt.n(l.quantityKg)));
        b.tvCost.setText(c.getString(R.string.fmt_cost, NumFmt.n(l.totalCost)));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemFeedLogBinding b;
        VH(ItemFeedLogBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}