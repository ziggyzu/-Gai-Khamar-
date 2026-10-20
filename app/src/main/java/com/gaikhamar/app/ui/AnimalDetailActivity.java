package com.gaikhamar.app.ui;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.databinding.ActivityAnimalDetailBinding;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.QrUtil;
import com.gaikhamar.app.viewmodel.AnimalViewModel;
import com.google.zxing.WriterException;

import java.util.Locale;

public class AnimalDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "animal_id";

    private ActivityAnimalDetailBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAnimalDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        AnimalViewModel vm = new ViewModelProvider(this).get(AnimalViewModel.class);
        vm.getAnimal(id).observe(this, a -> {
            if (a != null) show(a);
        });

        b.btnBack.setOnClickListener(v -> finish());
    }

    private void show(Animal a) {
        b.tvId.setText(a.farmCattleId);
        try {
            b.ivQr.setImageBitmap(QrUtil.generate(a.farmCattleId, 600));
        } catch (WriterException ignored) {
        }

        StringBuilder sb = new StringBuilder();
        line(sb, R.string.d_category, LabelHelper.category(this, a.category));
        line(sb, R.string.d_ear_tag, a.earTag);
        line(sb, R.string.d_name, a.name);
        line(sb, R.string.d_breed, a.breed);
        line(sb, R.string.d_gender, LabelHelper.gender(this, a.gender));
        line(sb, R.string.d_dob, a.dateOfBirth);
        line(sb, R.string.d_purchase_date, a.purchaseDate);
        if (a.purchasePrice > 0) {
            line(sb, R.string.d_purchase_price, getString(R.string.fmt_money,
                    String.format(Locale.US, "%,.0f", a.purchasePrice)));
        }
        if (a.currentWeightKg > 0) {
            line(sb, R.string.d_current_weight, getString(R.string.fmt_kg,
                    String.format(Locale.US, "%.1f", a.currentWeightKg)));
        }
        if (a.targetWeightKg > 0) {
            line(sb, R.string.d_target_weight, getString(R.string.fmt_kg,
                    String.format(Locale.US, "%.1f", a.targetWeightKg)));
        }
        line(sb, R.string.d_dairy_status, LabelHelper.dairyStatus(this, a.dairyStatus));
        line(sb, R.string.d_mother, a.motherId);
        line(sb, R.string.d_father, a.fatherId);
        line(sb, R.string.d_health, LabelHelper.health(this, a.healthStatus));
        line(sb, R.string.d_status, LabelHelper.status(this, a.status));
        line(sb, R.string.d_notes, a.notes);
        b.tvDetails.setText(sb.toString());
    }

    private void line(StringBuilder sb, int labelRes, String value) {
        if (value == null || value.trim().isEmpty()) return;
        sb.append(getString(labelRes)).append(": ").append(value).append("\n");
    }
}