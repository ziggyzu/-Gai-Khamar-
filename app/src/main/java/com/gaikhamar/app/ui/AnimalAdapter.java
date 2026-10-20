package com.gaikhamar.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.databinding.ItemAnimalBeefBinding;
import com.gaikhamar.app.databinding.ItemAnimalDairyBinding;
import com.gaikhamar.app.util.LabelHelper;

import java.util.Locale;
import java.util.Objects;

public class AnimalAdapter extends ListAdapter<Animal, RecyclerView.ViewHolder> {

    public interface OnAnimalClick {
        void onClick(Animal animal);
    }

    private static final int TYPE_BEEF = 0;
    private static final int TYPE_DAIRY = 1;

    private static final DiffUtil.ItemCallback<Animal> DIFF = new DiffUtil.ItemCallback<Animal>() {
        @Override
        public boolean areItemsTheSame(@NonNull Animal o, @NonNull Animal n) {
            return o.id == n.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Animal o, @NonNull Animal n) {
            return o.farmCattleId.equals(n.farmCattleId)
                    && o.healthStatus.equals(n.healthStatus)
                    && o.currentWeightKg == n.currentWeightKg
                    && Objects.equals(o.dairyStatus, n.dairyStatus)
                    && Objects.equals(o.breed, n.breed)
                    && Objects.equals(o.earTag, n.earTag);
        }
    };

    private final OnAnimalClick listener;

    public AnimalAdapter(OnAnimalClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return "DAIRY".equals(getItem(position).category) ? TYPE_DAIRY : TYPE_BEEF;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DAIRY) {
            return new DairyHolder(ItemAnimalDairyBinding.inflate(inf, parent, false));
        }
        return new BeefHolder(ItemAnimalBeefBinding.inflate(inf, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Animal a = getItem(position);
        if (holder instanceof DairyHolder) {
            bindDairy((DairyHolder) holder, a);
        } else {
            bindBeef((BeefHolder) holder, a);
        }
    }

    private void bindBeef(BeefHolder h, Animal a) {
        ItemAnimalBeefBinding b = h.b;
        Context c = b.getRoot().getContext();
        b.tvId.setText(a.farmCattleId);
        String type = "CALF".equals(a.category) ? c.getString(R.string.fmt_calf_suffix) : "";
        b.tvBreed.setText(a.breed + type);
        b.tvWeight.setText(c.getString(R.string.fmt_weight,
                String.format(Locale.US, "%.0f", a.currentWeightKg)));
        b.tvCost.setText(c.getString(R.string.fmt_cost,
                String.format(Locale.US, "%,.0f", a.purchasePrice)));
        applyHealth(c, b.tvHealth, a.healthStatus);
        b.getRoot().setOnClickListener(v -> listener.onClick(a));
    }

    private void bindDairy(DairyHolder h, Animal a) {
        ItemAnimalDairyBinding b = h.b;
        Context c = b.getRoot().getContext();
        b.tvId.setText(a.farmCattleId);
        b.tvBreed.setText(a.breed);
        b.tvStatus.setText(a.dairyStatus == null ? "" :
                c.getString(R.string.fmt_status, LabelHelper.dairyStatus(c, a.dairyStatus)));
        String tag = a.earTag == null || a.earTag.isEmpty() ? "-" : a.earTag;
        b.tvTag.setText(c.getString(R.string.fmt_tag, tag));
        applyHealth(c, b.tvHealth, a.healthStatus);
        b.getRoot().setOnClickListener(v -> listener.onClick(a));
    }

    private void applyHealth(Context c, TextView tv, String health) {
        tv.setText(LabelHelper.health(c, health));
        int color = "HEALTHY".equals(health) ? R.color.green_700 : R.color.red_600;
        tv.setTextColor(ContextCompat.getColor(c, color));
    }

    static class BeefHolder extends RecyclerView.ViewHolder {
        final ItemAnimalBeefBinding b;
        BeefHolder(ItemAnimalBeefBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    static class DairyHolder extends RecyclerView.ViewHolder {
        final ItemAnimalDairyBinding b;
        DairyHolder(ItemAnimalDairyBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}