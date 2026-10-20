package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;


import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.databinding.ActivityAnimalListBinding;
import com.gaikhamar.app.viewmodel.AnimalViewModel;

public class AnimalListActivity extends BaseActivity {

    private ActivityAnimalListBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAnimalListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        AnimalViewModel vm = new ViewModelProvider(this).get(AnimalViewModel.class);

        AnimalAdapter adapter = new AnimalAdapter(animal -> {
            Intent i = new Intent(this, AnimalDetailActivity.class);
            i.putExtra(AnimalDetailActivity.EXTRA_ID, animal.id);
            startActivity(i);
        });
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        vm.animals.observe(this, list -> {
            adapter.submitList(list);
            b.tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        b.chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            String cat = id == R.id.chipBeef ? "BEEF"
                    : id == R.id.chipDairy ? "DAIRY"
                    : id == R.id.chipCalf ? "CALF" : "ALL";
            vm.setCategory(cat);
        });

        b.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b2, int c) {
                vm.setQuery(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        b.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddAnimalActivity.class)));
    }
}