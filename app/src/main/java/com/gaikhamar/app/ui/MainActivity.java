package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.gaikhamar.app.R;
import com.gaikhamar.app.databinding.ActivityMainBinding;
import com.gaikhamar.app.util.LocaleHelper;
import com.gaikhamar.app.util.SessionManager;
import com.gaikhamar.app.viewmodel.AnimalViewModel;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.tvWelcome.setText(getString(R.string.welcome_user, session.getName()));
        b.tvFarm.setText(session.getFarmName());

        b.btnEnglish.setOnClickListener(v -> switchLanguage(LocaleHelper.EN));
        b.btnBangla.setOnClickListener(v -> switchLanguage(LocaleHelper.BN));

        AnimalViewModel vm = new ViewModelProvider(this).get(AnimalViewModel.class);
        vm.totalCount.observe(this, n -> b.tileTotal.setCount(n == null ? 0 : n));
        vm.beefCount.observe(this, n -> b.tileBeef.setCount(n == null ? 0 : n));
        vm.dairyCount.observe(this, n -> b.tileDairy.setCount(n == null ? 0 : n));
        vm.calfCount.observe(this, n -> b.tileCalf.setCount(n == null ? 0 : n));
        vm.sickCount.observe(this, n -> b.tileSick.setCount(n == null ? 0 : n));
        vm.pregnantCount.observe(this, n -> b.tilePregnant.setCount(n == null ? 0 : n));

        b.btnAnimals.setOnClickListener(v ->
                startActivity(new Intent(this, AnimalListActivity.class)));
        b.btnAddAnimal.setOnClickListener(v ->
                startActivity(new Intent(this, AddAnimalActivity.class)));
        b.btnLogout.setOnClickListener(v -> {
            session.logout();
            goToLogin();
        });
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}