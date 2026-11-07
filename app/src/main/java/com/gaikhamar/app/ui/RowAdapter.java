package com.gaikhamar.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gaikhamar.app.databinding.ItemRowBinding;

import java.util.Objects;

public class RowAdapter extends ListAdapter<Row, RowAdapter.VH> {

    public interface OnRowClick {
        void onClick(Row row);
    }

    private static final DiffUtil.ItemCallback<Row> DIFF = new DiffUtil.ItemCallback<Row>() {
        @Override
        public boolean areItemsTheSame(@NonNull Row o, @NonNull Row n) { return o.id == n.id; }

        @Override
        public boolean areContentsTheSame(@NonNull Row o, @NonNull Row n) {
            return Objects.equals(o.title, n.title) && Objects.equals(o.subtitle, n.subtitle)
                    && Objects.equals(o.right, n.right) && Objects.equals(o.rightSub, n.rightSub)
                    && o.rightColor == n.rightColor;
        }
    };

    private final OnRowClick listener;

    public RowAdapter(OnRowClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Row r = getItem(position);
        ItemRowBinding b = h.b;
        b.tvTitle.setText(r.title);
        b.tvSubtitle.setText(r.subtitle);
        b.tvSubtitle.setVisibility(r.subtitle == null || r.subtitle.isEmpty() ? View.GONE : View.VISIBLE);
        b.tvRight.setText(r.right);
        b.tvRight.setTextColor(r.rightColor != 0 ? r.rightColor : h.defaultColor);
        b.tvRightSub.setText(r.rightSub);
        b.tvRightSub.setVisibility(r.rightSub == null || r.rightSub.isEmpty() ? View.GONE : View.VISIBLE);
        b.getRoot().setOnClickListener(listener == null ? null : v -> listener.onClick(r));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemRowBinding b;
        final int defaultColor;

        VH(ItemRowBinding b) {
            super(b.getRoot());
            this.b = b;
            this.defaultColor = b.tvRight.getCurrentTextColor();
        }
    }
}