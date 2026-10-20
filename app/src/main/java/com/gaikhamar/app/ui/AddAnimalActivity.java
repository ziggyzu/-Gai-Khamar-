package com.gaikhamar.app.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.databinding.ActivityAddAnimalBinding;
import com.gaikhamar.app.util.LabelHelper;
import com.gaikhamar.app.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddAnimalActivity extends BaseActivity {

    // Codes saved in the database. The spinner shows translated labels for them.
    private static final String[] DAIRY_CODES = {"HEIFER", "MILKING", "DRY", "PREGNANT"};

    private ActivityAddAnimalBinding b;
    private Repository repo;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddAnimalBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = Repository.get(this);
        session = new SessionManager(this);

        String[] labels = new String[DAIRY_CODES.length];
        for (int i = 0; i < DAIRY_CODES.length; i++) {
            labels[i] = LabelHelper.dairyStatus(this, DAIRY_CODES[i]);
        }
        b.spDairyStatus.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, labels));

        b.rgCategory.setOnCheckedChangeListener((g, id) -> updateSections());
        updateSections();

        b.etDob.setOnClickListener(v -> pickDate(b.etDob));
        b.etPurchaseDate.setOnClickListener(v -> pickDate(b.etPurchaseDate));
        b.btnSave.setOnClickListener(v -> save());
    }

    private void updateSections() {
        int id = b.rgCategory.getCheckedRadioButtonId();
        b.groupBeef.setVisibility(id == R.id.rbBeef ? View.VISIBLE : View.GONE);
        b.groupDairy.setVisibility(id == R.id.rbDairy ? View.VISIBLE : View.GONE);
    }

    private void pickDate(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, y, m, d) -> target.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String breed = text(b.etBreed);
        if (breed.isEmpty()) {
            Toast.makeText(this, R.string.err_breed, Toast.LENGTH_SHORT).show();
            return;
        }

        Animal a = new Animal();
        int cat = b.rgCategory.getCheckedRadioButtonId();
        a.category = cat == R.id.rbDairy ? "DAIRY" : cat == R.id.rbCalf ? "CALF" : "BEEF";
        a.gender = b.rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "MALE" : "FEMALE";
        a.earTag = text(b.etEarTag);
        a.name = text(b.etAnimalName);
        a.breed = breed;
        a.dateOfBirth = text(b.etDob);
        a.purchaseDate = text(b.etPurchaseDate);
        a.purchasePrice = num(b.etPurchasePrice);
        a.currentWeightKg = num(b.etCurrentWeight);
        a.motherId = text(b.etMotherId);
        a.fatherId = text(b.etFatherId);
        a.notes = text(b.etNotes);

        if ("BEEF".equals(a.category)) {
            a.targetWeightKg = num(b.etTargetWeight);
        } else if ("DAIRY".equals(a.category)) {
            a.dairyStatus = DAIRY_CODES[b.spDairyStatus.getSelectedItemPosition()];
        }

        repo.addAnimal(a, session.getFarmCode(), saved -> {
            Toast.makeText(this, getString(R.string.saved_with_id, saved.farmCattleId),
                    Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, AnimalDetailActivity.class);
            i.putExtra(AnimalDetailActivity.EXTRA_ID, saved.id);
            startActivity(i);
            finish();
        });
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private double num(TextInputEditText e) {
        try {
            return Double.parseDouble(text(e));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}