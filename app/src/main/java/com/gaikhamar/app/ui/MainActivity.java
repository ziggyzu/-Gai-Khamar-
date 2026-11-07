package com.gaikhamar.app.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.gaikhamar.app.R;
import com.gaikhamar.app.databinding.ActivityMainBinding;
import com.gaikhamar.app.system.AlarmScheduler;
import com.gaikhamar.app.system.FarmMonitorService;
import com.gaikhamar.app.system.NotificationHelper;
import com.gaikhamar.app.system.ReminderReceiver;
import com.gaikhamar.app.system.SystemEventsReceiver;
import com.gaikhamar.app.util.LocaleHelper;
import com.gaikhamar.app.util.NumFmt;
import com.gaikhamar.app.util.SessionManager;
import com.gaikhamar.app.viewmodel.AnimalViewModel;
import com.gaikhamar.app.viewmodel.FeedViewModel;
import com.gaikhamar.app.viewmodel.FinanceViewModel;
import com.gaikhamar.app.viewmodel.HealthViewModel;
import com.gaikhamar.app.viewmodel.MilkViewModel;

public class MainActivity extends BaseActivity {

    private static final String NOTIF_PERMISSION = "android.permission.POST_NOTIFICATIONS";

    private ActivityMainBinding b;
    private double todayCost = 0, monthIncome = 0, monthExpense = 0;
    private int animalCount = 0;
    private boolean airplane = false, batteryLow = false, receiverRegistered = false;
    private SystemEventsReceiver systemReceiver;

    private final ActivityResultLauncher<String> notifPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> { });

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

        // Notifications, daily reminder, and the permission (Android 13 and newer)
        NotificationHelper.createChannels(this);
        AlarmScheduler.scheduleDaily(this);
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, NOTIF_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(NOTIF_PERMISSION);
        }
        // For testing: long-press the welcome text to fire the reminder right now
        b.tvWelcome.setOnLongClickListener(v -> {
            sendBroadcast(new Intent(this, ReminderReceiver.class));
            return true;
        });

        // Airplane mode and battery events
        airplane = Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        systemReceiver = new SystemEventsReceiver(new SystemEventsReceiver.Listener() {
            @Override public void onAirplane(boolean on) { airplane = on; updateBanner(); }
            @Override public void onBatteryLow(boolean low) { batteryLow = low; updateBanner(); }
        });
        updateBanner();

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
        vm.calvingSoonCount.observe(this, n -> b.tileCalvingSoon.setCount(n == null ? 0 : n));

        new ViewModelProvider(this).get(MilkViewModel.class).todayLiters.observe(this, l ->
                b.tileMilkToday.setValueText(NumFmt.n(l == null ? 0 : l)));

        new ViewModelProvider(this).get(HealthViewModel.class).vaccinesDue.observe(this, n ->
                b.tileVaccines.setCount(n == null ? 0 : n));

        FinanceViewModel fin = new ViewModelProvider(this).get(FinanceViewModel.class);
        fin.income.observe(this, v -> { monthIncome = v == null ? 0 : v; updateProfit(); });
        fin.expense.observe(this, v -> { monthExpense = v == null ? 0 : v; updateProfit(); });

        FeedViewModel fvm = new ViewModelProvider(this).get(FeedViewModel.class);
        fvm.todayKg.observe(this, kg -> b.tileFeedToday.setValueText(NumFmt.n(kg == null ? 0 : kg)));
        fvm.todayCost.observe(this, cost -> {
            todayCost = cost == null ? 0 : cost;
            updateCostPerAnimal();
        });
        fvm.lowStockCount.observe(this, n -> {
            int count = n == null ? 0 : n;
            b.tileLowStock.setCount(count);
            if (count > 0) {
                b.tvLowStockWarning.setText(getString(R.string.low_stock_warning, String.valueOf(count)));
                b.tvLowStockWarning.setVisibility(View.VISIBLE);
            } else {
                b.tvLowStockWarning.setVisibility(View.GONE);
            }
        });

        b.tvLowStockWarning.setOnClickListener(v -> open(FeedStockActivity.class));
        b.btnAnimals.setOnClickListener(v -> open(AnimalListActivity.class));
        b.btnAddAnimal.setOnClickListener(v -> open(AddAnimalActivity.class));
        b.btnMilk.setOnClickListener(v -> open(MilkActivity.class));
        b.btnBatches.setOnClickListener(v -> open(BatchActivity.class));
        b.btnFeedLog.setOnClickListener(v -> open(FeedLogActivity.class));
        b.btnFeedStock.setOnClickListener(v -> open(FeedStockActivity.class));
        b.btnVaccines.setOnClickListener(v -> open(VaccinationActivity.class));
        b.btnFinance.setOnClickListener(v -> open(FinanceActivity.class));
        b.btnCustomers.setOnClickListener(v -> open(CustomersActivity.class));
        b.btnProfitReport.setOnClickListener(v -> open(ProfitActivity.class));
        b.btnContacts.setOnClickListener(v -> open(ContactsActivity.class));

        b.btnMonitor.setOnClickListener(v -> {
            Intent i = new Intent(this, FarmMonitorService.class);
            boolean willRun = !FarmMonitorService.isRunning();
            if (willRun) ContextCompat.startForegroundService(this, i); else stopService(i);
            b.btnMonitor.setText(willRun ? R.string.monitor_stop : R.string.monitor_start);
        });

        b.btnLogout.setOnClickListener(v -> {
            session.logout();
            goToLogin();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (b != null && !receiverRegistered) {
            registerReceiver(systemReceiver, SystemEventsReceiver.filter());
            receiverRegistered = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (b != null) {
            b.btnMonitor.setText(FarmMonitorService.isRunning() ? R.string.monitor_stop : R.string.monitor_start);
        }
    }

    @Override
    protected void onStop() {
        if (receiverRegistered) {
            unregisterReceiver(systemReceiver);
            receiverRegistered = false;
        }
        super.onStop();
    }

    private void open(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    private void updateBanner() {
        StringBuilder s = new StringBuilder();
        if (airplane) s.append(getString(R.string.banner_airplane));
        if (batteryLow) {
            if (s.length() > 0) s.append("\n");
            s.append(getString(R.string.banner_battery));
        }
        b.tvSystemBanner.setText(s);
        b.tvSystemBanner.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
    }

    private void updateProfit() {
        b.tileProfit.setValueText(NumFmt.n(monthIncome - monthExpense));
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