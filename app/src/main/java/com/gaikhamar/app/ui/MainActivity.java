package com.gaikhamar.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.gaikhamar.app.R;
import com.gaikhamar.app.databinding.ActivityMainBinding;
import com.gaikhamar.app.util.LocaleHelper;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.util.SessionManager;
import com.gaikhamar.app.viewmodel.AnimalViewModel;
import com.gaikhamar.app.viewmodel.FeedViewModel;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding b;
    private double todayCost = 0;
    private int animalCount = 0;

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
        vm.totalCount.observe(this, n -> {
            animalCount = n == null ? 0 : n;
            b.tileTotal.setCount(animalCount);
            updateCostPerAnimal();
        });
        vm.beefCount.observe(this, n -> b.tileBeef.setCount(n == null ? 0 : n));
        vm.dairyCount.observe(this, n -> b.tileDairy.setCount(n == null ? 0 : n));
        vm.calfCount.observe(this, n -> b.tileCalf.setCount(n == null ? 0 : n));
        vm.sickCount.observe(this, n -> b.tileSick.setCount(n == null ? 0 : n));
        vm.pregnantCount.observe(this, n -> b.tilePregnant.setCount(n == null ? 0 : n));

        FeedViewModel fvm = new ViewModelProvider(this).get(FeedViewModel.class);
        fvm.todayKg.observe(this, kg ->
                b.tileFeedToday.setValueText(NumFmt.n(kg == null ? 0 : kg)));
        fvm.todayCost.observe(this, cost -> {
            todayCost = cost == null ? 0 : cost;
            updateCostPerAnimal();
        });
        fvm.lowStockCount.observe(this, n -> {
            int count = n == null ? 0 : n;
            b.tileLowStock.setCount(count);
            if (count > 0) {
                b.tvLowStockWarning.setText(
                        getString(R.string.low_stock_warning, String.valueOf(count)));
                b.tvLowStockWarning.setVisibility(View.VISIBLE);
            } else {
                b.tvLowStockWarning.setVisibility(View.GONE);
            }
        });

        b.tvLowStockWarning.setOnClickListener(v ->
                startActivity(new Intent(this, FeedStockActivity.class)));
        b.btnAnimals.setOnClickListener(v ->
                startActivity(new Intent(this, AnimalListActivity.class)));
        b.btnAddAnimal.setOnClickListener(v ->
                startActivity(new Intent(this, AddAnimalActivity.class)));
        b.btnFeedLog.setOnClickListener(v ->
                startActivity(new Intent(this, FeedLogActivity.class)));
        b.btnFeedStock.setOnClickListener(v ->
                startActivity(new Intent(this, FeedStockActivity.class)));
        b.btnLogout.setOnClickListener(v -> {
            session.logout();
            goToLogin();
        });
    }

    private void updateCostPerAnimal() {
        double per = animalCount > 0 ? todayCost / animalCount : 0;
        b.tvCostPerAnimal.setText(getString(R.string.cost_per_animal, NumFmt.n(per)));
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}