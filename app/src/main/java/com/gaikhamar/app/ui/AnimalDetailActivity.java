package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.data.entity.Batch;
import com.gaikhamar.app.data.entity.BreedingRecord;
import com.gaikhamar.app.data.entity.HealthRecord;
import com.gaikhamar.app.data.entity.WeightRecord;
import com.gaikhamar.app.databinding.ActivityAnimalDetailBinding;
import com.gaikhamar.app.util.DateUtil;
import com.gaikhamar.app.util.FormDialog;
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
    private List<HealthRecord> health;
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
        repo.healthRecords(animalId).observe(this, l -> { health = l; refresh(); });
        bvm.batches.observe(this, l -> { batches = l; refresh(); });

        b.btnBack.setOnClickListener(v -> finish());

        b.btnAddWeight.setOnClickListener(v -> showAddWeight());
        b.btnSalePrice.setOnClickListener(v -> showSalePrice());
        b.btnAssignBatch.setOnClickListener(v -> showAssignBatch());

        b.btnHeat.setOnClickListener(v -> showHeat());
        b.btnInsemination.setOnClickListener(v -> showInsemination());
        b.btnPregCheck.setOnClickListener(v -> showPregnancyCheck());
        b.btnCalving.setOnClickListener(v -> showCalving());
        b.btnDryOff.setOnClickListener(v -> showDryOff());

        b.btnHealthRecord.setOnClickListener(v -> showHealthRecord());
        b.btnQuarantine.setOnClickListener(v -> repo.toggleQuarantine(animalId, ok -> toast(R.string.quarantine_updated)));
        b.btnRecovered.setOnClickListener(v -> repo.markRecovered(animalId, ok -> toast(R.string.recovered_done)));
        b.btnSell.setOnClickListener(v -> showSell());
    }

    // ---------------------------------------------------------------- screen

    private void refresh() {
        if (animal == null) return;
        Animal a = animal;
        boolean dairy = "DAIRY".equals(a.category);
        boolean active = "ACTIVE".equals(a.status);

        b.tvId.setText(a.farmCattleId);
        if (!qrShown) {
            try {
                b.ivQr.setImageBitmap(QrUtil.generate(a.farmCattleId, 600));
                qrShown = true;
            } catch (WriterException ignored) {
            }
        }

        b.groupBeefActions.setVisibility(active && !dairy ? View.VISIBLE : View.GONE);
        b.groupDairyActions.setVisibility(active && dairy ? View.VISIBLE : View.GONE);
        b.groupCommonActions.setVisibility(active ? View.VISIBLE : View.GONE);
        b.btnQuarantine.setText("QUARANTINE".equals(a.healthStatus) ? R.string.quarantine_end : R.string.quarantine_start);
        b.btnRecovered.setVisibility("HEALTHY".equals(a.healthStatus) ? View.GONE : View.VISIBLE);

        // Details
        StringBuilder sb = new StringBuilder();
        line(sb, R.string.d_category, LabelHelper.category(this, a.category));
        line(sb, R.string.d_ear_tag, a.earTag);
        line(sb, R.string.d_name, a.name);
        line(sb, R.string.d_breed, a.breed);
        line(sb, R.string.d_gender, LabelHelper.gender(this, a.gender));
        line(sb, R.string.d_dob, a.dateOfBirth);
        line(sb, R.string.d_purchase_date, a.purchaseDate);
        if (a.purchasePrice > 0) line(sb, R.string.d_purchase_price, getString(R.string.fmt_money, NumFmt.n(a.purchasePrice)));
        if (a.currentWeightKg > 0) line(sb, R.string.d_current_weight, getString(R.string.fmt_kg, NumFmt.n(a.currentWeightKg)));
        if (a.targetWeightKg > 0) line(sb, R.string.d_target_weight, getString(R.string.fmt_kg, NumFmt.n(a.targetWeightKg)));
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
        commonInsights(a, in);
        b.tvInsights.setText(in.toString().trim());
        int vis = in.length() == 0 ? View.GONE : View.VISIBLE;
        b.tvInsightsTitle.setVisibility(vis);
        b.tvInsights.setVisibility(vis);

        // History
        StringBuilder h = new StringBuilder();
        if (weights != null) {
            for (int i = 0; i < Math.min(8, weights.size()); i++) {
                WeightRecord w = weights.get(i);
                h.append(w.date).append("  -  ").append(getString(R.string.fmt_kg, NumFmt.n(w.weightKg))).append("\n");
            }
        }
        if (breeding != null) {
            for (int i = 0; i < Math.min(8, breeding.size()); i++) {
                BreedingRecord r = breeding.get(i);
                h.append(r.date).append("  -  ").append(LabelHelper.breedingType(this, r.type));
                if (notEmpty(r.notes)) h.append(" (").append(r.notes).append(")");
                h.append("\n");
            }
        }
        if (health != null) {
            for (int i = 0; i < Math.min(8, health.size()); i++) {
                HealthRecord r = health.get(i);
                h.append(r.date).append("  -  ").append(LabelHelper.condition(this, r.condition));
                if (notEmpty(r.treatment)) h.append(" (").append(r.treatment).append(")");
                h.append("  -  ").append(getString("ONGOING".equals(r.status) ? R.string.hs_ongoing : R.string.hs_recovered));
                h.append("\n");
            }
        }
        b.tvHistory.setText(h.length() == 0 ? getString(R.string.no_history) : h.toString().trim());
    }

    private void beefInsights(Animal a, StringBuilder in) {
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
                in.append(getString(R.string.i_weight_left, NumFmt.n(a.targetWeightKg - a.currentWeightKg))).append("\n");
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
        if (milk7 > 0) in.append(getString(R.string.i_milk7, NumFmt.n(milk7 / 7.0))).append("\n");
        if (notEmpty(a.lastHeatDate)) {
            in.append(getString(R.string.i_next_heat, DateUtil.plusDays(a.lastHeatDate, 21))).append("\n");
        }
        if (notEmpty(a.lastInseminationDate)) in.append(getString(R.string.i_last_insem, a.lastInseminationDate)).append("\n");
        if (notEmpty(a.expectedCalvingDate)) {
            long left = DateUtil.daysBetween(today, a.expectedCalvingDate);
            if (left >= 0) {
                in.append(getString(R.string.i_calving, a.expectedCalvingDate, String.valueOf(left))).append("\n");
                in.append(getString(R.string.i_dry_suggest, DateUtil.plusDays(a.expectedCalvingDate, -60))).append("\n");
            } else {
                in.append(getString(R.string.i_calving_overdue, a.expectedCalvingDate)).append("\n");
            }
        }
        if ("DRY".equals(a.dairyStatus) && notEmpty(a.dryOffDate)) in.append(getString(R.string.i_dry_since, a.dryOffDate)).append("\n");
    }

    private void commonInsights(Animal a, StringBuilder in) {
        if (notEmpty(a.withdrawalUntil) && a.withdrawalUntil.compareTo(DateUtil.today()) >= 0) {
            in.append(getString(R.string.i_withdrawal, a.withdrawalUntil)).append("\n");
        }
        if (health != null) {
            for (HealthRecord r : health) {
                if ("ONGOING".equals(r.status) && notEmpty(r.followUpDate)) {
                    in.append(getString(R.string.i_followup, r.followUpDate)).append("\n");
                    break;
                }
            }
        }
    }

    private boolean notEmpty(String s) { return s != null && !s.isEmpty(); }

    private String batchName(long id) {
        if (batches != null) {
            for (Batch bt : batches) if (bt.id == id) return bt.name;
        }
        return String.valueOf(id);
    }

    private void line(StringBuilder sb, int labelRes, String value) {
        if (value == null || value.trim().isEmpty()) return;
        sb.append(getString(labelRes)).append(": ").append(value).append("\n");
    }

    private void toast(int res) { Toast.makeText(this, res, Toast.LENGTH_SHORT).show(); }

    // --------------------------------------------------------------- beef

    private void showAddWeight() {
        new FormDialog.Builder(this, R.string.add_weight)
                .date("date", R.string.date)
                .number("kg", R.string.weight_kg)
                .show(v -> {
                    if (v.num("kg") <= 0) { toast(R.string.err_weight); return false; }
                    repo.addWeight(animalId, v.str("date"), v.num("kg"), ok -> toast(R.string.weight_saved));
                    return true;
                });
    }

    private void showSalePrice() {
        String preset = animal != null && animal.expectedPricePerKg > 0 ? NumFmt.plain(animal.expectedPricePerKg) : null;
        new FormDialog.Builder(this, R.string.set_sale_price)
                .number("price", R.string.price_per_kg_live, preset)
                .show(v -> {
                    repo.setExpectedPrice(animalId, v.num("price"), ok -> toast(R.string.price_saved));
                    return true;
                });
    }

    private void showAssignBatch() {
        if (batches == null || batches.isEmpty()) { toast(R.string.err_no_batches); return; }
        String[] labels = new String[batches.size() + 1];
        labels[0] = getString(R.string.no_batch);
        int selected = 0;
        for (int i = 0; i < batches.size(); i++) {
            labels[i + 1] = batches.get(i).name;
            if (animal != null && batches.get(i).id == animal.batchId) selected = i + 1;
        }
        new FormDialog.Builder(this, R.string.assign_batch)
                .choice("batch", R.string.d_batch, labels, selected)
                .show(v -> {
                    int pos = v.choice("batch");
                    long id = pos == 0 ? 0 : batches.get(pos - 1).id;
                    repo.assignBatch(animalId, id, ok -> toast(R.string.batch_assigned));
                    return true;
                });
    }

    // -------------------------------------------------------------- dairy

    private void showHeat() {
        new FormDialog.Builder(this, R.string.record_heat)
                .date("date", R.string.date)
                .show(v -> {
                    repo.addBreeding(animalId, "HEAT", v.str("date"), "", r -> toast(R.string.saved_ok));
                    return true;
                });
    }

    private void showInsemination() {
        new FormDialog.Builder(this, R.string.record_insemination)
                .date("date", R.string.date)
                .text("notes", R.string.bull_semen)
                .show(v -> {
                    repo.addBreeding(animalId, "INSEMINATION", v.str("date"), v.str("notes"), r -> toast(R.string.saved_ok));
                    return true;
                });
    }

    private void showPregnancyCheck() {
        String[] results = {getString(R.string.pregnant), getString(R.string.preg_no)};
        new FormDialog.Builder(this, R.string.pregnancy_check)
                .date("date", R.string.date)
                .choice("res", R.string.pregnancy_check, results)
                .show(v -> {
                    String type = v.choice("res") == 0 ? "PREGNANCY_POSITIVE" : "PREGNANCY_NEGATIVE";
                    repo.addBreeding(animalId, type, v.str("date"), "",
                            r -> toast(r == 1 ? R.string.preg_no_insem : R.string.saved_ok));
                    return true;
                });
    }

    private void showCalving() {
        String[] genders = new String[GENDER_CODES.length];
        for (int i = 0; i < GENDER_CODES.length; i++) genders[i] = LabelHelper.gender(this, GENDER_CODES[i]);
        new FormDialog.Builder(this, R.string.record_calving)
                .date("date", R.string.date)
                .choice("gender", R.string.gender, genders)
                .number("kg", R.string.birth_weight)
                .text("tag", R.string.calf_tag)
                .show(v -> {
                    repo.recordCalving(animalId, session.getFarmCode(), v.str("date"),
                            GENDER_CODES[v.choice("gender")], v.num("kg"), v.str("tag"), calf -> {
                                if (calf != null) {
                                    Toast.makeText(this, getString(R.string.calving_saved, calf.farmCattleId),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                    return true;
                });
    }

    private void showDryOff() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dry_off)
                .setMessage(R.string.dry_off_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, (d, w) ->
                        repo.dryOff(animalId, DateUtil.today(), ok -> toast(R.string.dry_off_done)))
                .show();
    }

    // ------------------------------------------------------ health and sale

    private void showHealthRecord() {
        String[] codes = getResources().getStringArray(R.array.condition_codes);
        String[] labels = getResources().getStringArray(R.array.condition_labels);
        new FormDialog.Builder(this, R.string.health_record)
                .date("date", R.string.date)
                .choice("cond", R.string.condition, labels)
                .text("symptoms", R.string.symptoms)
                .number("temp", R.string.temperature)
                .text("treatment", R.string.treatment)
                .text("vet", R.string.vet_name)
                .number("cost", R.string.treatment_cost)
                .number("wd", R.string.withdrawal_days)
                .number("follow", R.string.followup_days)
                .show(v -> {
                    String date = v.str("date");
                    int follow = (int) v.num("follow");
                    repo.addHealthRecord(animalId, date, codes[v.choice("cond")], v.str("symptoms"),
                            v.num("temp"), v.str("treatment"), v.str("vet"), v.num("cost"),
                            (int) v.num("wd"), follow > 0 ? DateUtil.plusDays(date, follow) : "",
                            ok -> toast(R.string.saved_ok));
                    return true;
                });
    }

    private void showSell() {
        new FormDialog.Builder(this, R.string.sell_animal)
                .date("date", R.string.date)
                .number("price", R.string.sale_price)
                .text("buyer", R.string.buyer)
                .show(v -> {
                    if (v.num("price") <= 0) { toast(R.string.err_price); return false; }
                    repo.sellAnimal(animalId, v.num("price"), v.str("buyer"), v.str("date"), ok -> toast(R.string.sold_done));
                    return true;
                });
    }
}