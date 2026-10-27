package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.data.entity.Batch;
import com.gaikhamar.app.data.entity.BreedingRecord;
import com.gaikhamar.app.data.entity.WeightRecord;
import com.gaikhamar.app.databinding.ActivityAnimalDetailBinding;
import com.gaikhamar.app.databinding.DialogAddWeightBinding;
import com.gaikhamar.app.databinding.DialogBreedingBinding;
import com.gaikhamar.app.databinding.DialogCalvingBinding;
import com.gaikhamar.app.databinding.DialogNumberBinding;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.InputUtil;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.util.QrUtil;
import com.gaikhamar.app.util.SessionManager;
import com.gaikhamar.app.viewmodel.AnimalViewModel;
import com.gaikhamar.app.viewmodel.BatchViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.WriterException;

import java.util.List;
import java.util.Locale;

public class AnimalDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "animal_id";
    private static final String[] GENDER_CODES = {"FEMALE", "MALE"};

    private ActivityAnimalDetailBinding b;
    private Repository repo;
    private SessionManager session;
    private long animalId;

    private Animal animal;
    private List<WeightRecord> weights;
    private List<BreedingRecord> breeding;
    private List<Batch> batches;
    private double milk7 = 0;
    private boolean qrShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAnimalDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = Repository.get(this);
        session = new SessionManager(this);
        animalId = getIntent().getLongExtra(EXTRA_ID, -1);

        AnimalViewModel vm = new ViewModelProvider(this).get(AnimalViewModel.class);
        BatchViewModel bvm = new ViewModelProvider(this).get(BatchViewModel.class);

        vm.getAnimal(animalId).observe(this, a -> { animal = a; refresh(); });
        vm.weights(animalId).observe(this, l -> { weights = l; refresh(); });
        vm.breeding(animalId).observe(this, l -> { breeding = l; refresh(); });
        vm.milkLast7(animalId).observe(this, v -> { milk7 = v == null ? 0 : v; refresh(); });
        bvm.batches.observe(this, l -> { batches = l; refresh(); });

        b.btnBack.setOnClickListener(v -> finish());

        // Beef and calf actions
        b.btnAddWeight.setOnClickListener(v -> showAddWeight());
        b.btnSalePrice.setOnClickListener(v -> showSalePrice());
        b.btnAssignBatch.setOnClickListener(v -> showAssignBatch());

        // Dairy actions
        b.btnHeat.setOnClickListener(v -> showBreedingDialog("HEAT"));
        b.btnInsemination.setOnClickListener(v -> showBreedingDialog("INSEMINATION"));
        b.btnPregCheck.setOnClickListener(v -> showBreedingDialog("PREGNANCY"));
        b.btnCalving.setOnClickListener(v -> showCalvingDialog());
        b.btnDryOff.setOnClickListener(v -> showDryOffDialog());
    }

    // ------------------------------------------------------------------
    // Screen content
    // ------------------------------------------------------------------

    private void refresh() {
        if (animal == null) return;
        Animal a = animal;
        boolean dairy = "DAIRY".equals(a.category);

        b.tvId.setText(a.farmCattleId);
        if (!qrShown) {
            try {
                b.ivQr.setImageBitmap(QrUtil.generate(a.farmCattleId, 600));
                qrShown = true;
            } catch (WriterException ignored) {
            }
        }

        b.groupBeefActions.setVisibility(dairy ? View.GONE : View.VISIBLE);
        b.groupDairyActions.setVisibility(dairy ? View.VISIBLE : View.GONE);

        // Details
        StringBuilder sb = new StringBuilder();
        line(sb, R.string.d_category, LabelHelper.category(this, a.category));
        line(sb, R.string.d_ear_tag, a.earTag);
        line(sb, R.string.d_name, a.name);
        line(sb, R.string.d_breed, a.breed);
        line(sb, R.string.d_gender, LabelHelper.gender(this, a.gender));
        line(sb, R.string.d_dob, a.dateOfBirth);
        line(sb, R.string.d_purchase_date, a.purchaseDate);
        if (a.purchasePrice > 0) {
            line(sb, R.string.d_purchase_price, getString(R.string.fmt_money, NumFmt.n(a.purchasePrice)));
        }
        if (a.currentWeightKg > 0) {
            line(sb, R.string.d_current_weight, getString(R.string.fmt_kg, NumFmt.n(a.currentWeightKg)));
        }
        if (a.targetWeightKg > 0) {
            line(sb, R.string.d_target_weight, getString(R.string.fmt_kg, NumFmt.n(a.targetWeightKg)));
        }
        line(sb, R.string.d_dairy_status, LabelHelper.dairyStatus(this, a.dairyStatus));
        if (a.batchId > 0) line(sb, R.string.d_batch, batchName(a.batchId));
        line(sb, R.string.d_mother, a.motherId);
        line(sb, R.string.d_father, a.fatherId);
        line(sb, R.string.d_health, LabelHelper.health(this, a.healthStatus));
        line(sb, R.string.d_status, LabelHelper.status(this, a.status));
        line(sb, R.string.d_notes, a.notes);
        b.tvDetails.setText(sb.toString());

        // Insights
        StringBuilder in = new StringBuilder();
        if (dairy) dairyInsights(a, in); else beefInsights(a, in);
        b.tvInsights.setText(in.toString().trim());
        b.tvInsightsTitle.setVisibility(in.length() == 0 ? View.GONE : View.VISIBLE);
        b.tvInsights.setVisibility(in.length() == 0 ? View.GONE : View.VISIBLE);

        // History
        StringBuilder h = new StringBuilder();
        if (weights != null) {
            for (int i = 0; i < Math.min(8, weights.size()); i++) {
                WeightRecord w = weights.get(i);
                h.append(w.date).append("  -  ")
                        .append(getString(R.string.fmt_kg, NumFmt.n(w.weightKg))).append("\n");
            }
        }
        if (breeding != null) {
            for (int i = 0; i < Math.min(8, breeding.size()); i++) {
                BreedingRecord r = breeding.get(i);
                h.append(r.date).append("  -  ").append(LabelHelper.breedingType(this, r.type));
                if (r.notes != null && !r.notes.isEmpty()) h.append(" (").append(r.notes).append(")");
                h.append("\n");
            }
        }
        b.tvHistory.setText(h.length() == 0 ? getString(R.string.no_history) : h.toString().trim());
    }

    private void beefInsights(Animal a, StringBuilder in) {
        // Average daily gain from the oldest to the newest weight record
        if (weights != null && weights.size() >= 2) {
            WeightRecord newest = weights.get(0);
            WeightRecord oldest = weights.get(weights.size() - 1);
            long days = DateUtil.daysBetween(oldest.date, newest.date);
            if (days > 0) {
                double gain = (newest.weightKg - oldest.weightKg) / days;
                in.append(getString(R.string.i_gain, String.format(Locale.US, "%.2f", gain))).append("\n");
            }
        }
        if (a.targetWeightKg > 0 && a.currentWeightKg > 0) {
            if (a.currentWeightKg >= a.targetWeightKg) {
                in.append(getString(R.string.i_target_reached)).append("\n");
            } else {
                in.append(getString(R.string.i_weight_left,
                        NumFmt.n(a.targetWeightKg - a.currentWeightKg))).append("\n");
            }
        }
        if (a.expectedPricePerKg > 0 && a.currentWeightKg > 0) {
            double value = a.currentWeightKg * a.expectedPricePerKg;
            in.append(getString(R.string.batch_value, NumFmt.n(value))).append("\n");
            in.append(getString(R.string.batch_profit, NumFmt.n(value - a.purchasePrice))).append("\n");
        }
    }

    private void dairyInsights(Animal a, StringBuilder in) {
        String today = DateUtil.today();
        if ("MILKING".equals(a.dairyStatus) && notEmpty(a.lactationStartDate)) {
            long days = DateUtil.daysBetween(a.lactationStartDate, today);
            in.append(getString(R.string.i_lactation, String.valueOf(Math.max(0, days)))).append("\n");
        }
        if (milk7 > 0) {
            in.append(getString(R.string.i_milk7, NumFmt.n(milk7 / 7.0))).append("\n");
        }
        if (notEmpty(a.lastHeatDate)) {
            in.append(getString(R.string.i_next_heat, DateUtil.plusDays(a.lastHeatDate, 21))).append("\n");
        }
        if (notEmpty(a.lastInseminationDate)) {
            in.append(getString(R.string.i_last_insem, a.lastInseminationDate)).append("\n");
        }
        if (notEmpty(a.expectedCalvingDate)) {
            long left = DateUtil.daysBetween(today, a.expectedCalvingDate);
            if (left >= 0) {
                in.append(getString(R.string.i_calving, a.expectedCalvingDate, String.valueOf(left))).append("\n");
                in.append(getString(R.string.i_dry_suggest,
                        DateUtil.plusDays(a.expectedCalvingDate, -60))).append("\n");
            } else {
                in.append(getString(R.string.i_calving_overdue, a.expectedCalvingDate)).append("\n");
            }
        }
        if ("DRY".equals(a.dairyStatus) && notEmpty(a.dryOffDate)) {
            in.append(getString(R.string.i_dry_since, a.dryOffDate)).append("\n");
        }
    }

    private boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private String batchName(long id) {
        if (batches != null) {
            for (Batch bt : batches) {
                if (bt.id == id) return bt.name;
            }
        }
        return String.valueOf(id);
    }

    private void line(StringBuilder sb, int labelRes, String value) {
        if (value == null || value.trim().isEmpty()) return;
        sb.append(getString(labelRes)).append(": ").append(value).append("\n");
    }

    private void toast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    // ------------------------------------------------------------------
    // Beef actions
    // ------------------------------------------------------------------

    private void showAddWeight() {
        DialogAddWeightBinding d = DialogAddWeightBinding.inflate(getLayoutInflater());
        InputUtil.dateField(this, d.etDate);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_weight)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double kg = InputUtil.num(d.etWeight);
            if (kg <= 0) {
                toast(R.string.err_weight);
                return;
            }
            repo.addWeight(animalId, InputUtil.text(d.etDate), kg, ok -> toast(R.string.weight_saved));
            dialog.dismiss();
        });
    }

    private void showSalePrice() {
        DialogNumberBinding d = DialogNumberBinding.inflate(getLayoutInflater());
        d.tilValue.setHint(getString(R.string.price_per_kg_live));
        if (animal != null && animal.expectedPricePerKg > 0) {
            d.etValue.setText(NumFmt.plain(animal.expectedPricePerKg));
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.set_sale_price)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double price = InputUtil.num(d.etValue);
            repo.setExpectedPrice(animalId, price, ok -> toast(R.string.price_saved));
            dialog.dismiss();
        });
    }

    private void showAssignBatch() {
        if (batches == null || batches.isEmpty()) {
            toast(R.string.err_no_batches);
            return;
        }
        String[] labels = new String[batches.size() + 1];
        labels[0] = getString(R.string.no_batch);
        int selected = 0;
        for (int i = 0; i < batches.size(); i++) {
            labels[i + 1] = batches.get(i).name;
            if (animal != null && batches.get(i).id == animal.batchId) selected = i + 1;
        }

        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
        sp.setSelection(selected);

        FrameLayout box = new FrameLayout(this);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, 0);
        box.addView(sp);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.assign_batch)
                .setView(box)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (dlg, which) -> {
                    int pos = sp.getSelectedItemPosition();
                    long id = pos == 0 ? 0 : batches.get(pos - 1).id;
                    repo.assignBatch(animalId, id, ok -> toast(R.string.batch_assigned));
                })
                .show();
    }

    // ------------------------------------------------------------------
    // Dairy actions
    // ------------------------------------------------------------------

    private void showBreedingDialog(String type) {
        DialogBreedingBinding d = DialogBreedingBinding.inflate(getLayoutInflater());
        InputUtil.dateField(this, d.etDate);
        d.tilNotes.setVisibility("INSEMINATION".equals(type) ? View.VISIBLE : View.GONE);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null);

        if ("PREGNANCY".equals(type)) {
            builder.setTitle(R.string.pregnancy_check)
                    .setPositiveButton(R.string.pregnant,
                            (dlg, w) -> saveBreeding("PREGNANCY_POSITIVE", d))
                    .setNeutralButton(R.string.preg_no,
                            (dlg, w) -> saveBreeding("PREGNANCY_NEGATIVE", d));
        } else {
            builder.setTitle("HEAT".equals(type) ? R.string.record_heat : R.string.record_insemination)
                    .setPositiveButton(R.string.save, (dlg, w) -> saveBreeding(type, d));
        }
        builder.show();
    }

    private void saveBreeding(String type, DialogBreedingBinding d) {
        String date = InputUtil.text(d.etDate);
        String notes = InputUtil.text(d.etNotes);
        repo.addBreeding(animalId, type, date, notes, result ->
                toast(result == 1 ? R.string.preg_no_insem : R.string.saved_ok));
    }

    private void showCalvingDialog() {
        DialogCalvingBinding d = DialogCalvingBinding.inflate(getLayoutInflater());
        InputUtil.dateField(this, d.etDate);

        String[] genderLabels = new String[GENDER_CODES.length];
        for (int i = 0; i < GENDER_CODES.length; i++) {
            genderLabels[i] = LabelHelper.gender(this, GENDER_CODES[i]);
        }
        d.spGender.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, genderLabels));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.record_calving)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (dlg, w) -> {
                    String gender = GENDER_CODES[d.spGender.getSelectedItemPosition()];
                    repo.recordCalving(animalId, session.getFarmCode(),
                            InputUtil.text(d.etDate), gender,
                            InputUtil.num(d.etBirthWeight), InputUtil.text(d.etCalfTag),
                            calf -> {
                                if (calf != null) {
                                    Toast.makeText(this,
                                            getString(R.string.calving_saved, calf.farmCattleId),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .show();
    }

    private void showDryOffDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dry_off)
                .setMessage(R.string.dry_off_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, (dlg, w) ->
                        repo.dryOff(animalId, DateUtil.today(), ok -> toast(R.string.dry_off_done)))
                .show();
    }
}