package com.gaikhamar.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.entity.MilkRecord;
import com.gaikhamar.app.databinding.ItemMilkRecordBinding;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.NumFmt;

public class MilkAdapter extends ListAdapter<MilkRecord, MilkAdapter.VH> {

    private static final DiffUtil.ItemCallback<MilkRecord> DIFF = new DiffUtil.ItemCallback<MilkRecord>() {
        @Override
        public boolean areItemsTheSame(@NonNull MilkRecord o, @NonNull MilkRecord n) {
            return o.id == n.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MilkRecord o, @NonNull MilkRecord n) {
            return o.liters == n.liters && o.session.equals(n.session)
                    && o.animalCode.equals(n.animalCode);
        }
    };

    public MilkAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemMilkRecordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MilkRecord r = getItem(position);
        Context c = h.b.getRoot().getContext();
        h.b.tvCow.setText(r.animalCode);
        h.b.tvSession.setText(LabelHelper.milkSession(c, r.session));
        h.b.tvLiters.setText(c.getString(R.string.fmt_liters, NumFmt.n(r.liters)));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemMilkRecordBinding b;
        VH(ItemMilkRecordBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}