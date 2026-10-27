package com.gaikhamar.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.FeedLog;
import com.gaikhamar.app.data.entity.FeedType;
import com.gaikhamar.app.util.DateUtil;

import java.util.List;

public class FeedViewModel extends AndroidViewModel {

    private final Repository repo;
    private final MutableLiveData<String> date = new MutableLiveData<>(DateUtil.today());

    public final LiveData<List<FeedType>> feedTypes;
    public final LiveData<Integer> lowStockCount;

    // For the date chosen on the feed log screen
    public final LiveData<List<FeedLog>> logs;
    public final LiveData<Double> totalKg;
    public final LiveData<Double> totalCost;

    // For today (dashboard)
    public final LiveData<Double> todayKg;
    public final LiveData<Double> todayCost;

    public FeedViewModel(@NonNull Application app) {
        super(app);
        repo = Repository.get(app);
        feedTypes = repo.feedTypes();
        lowStockCount = repo.countLowStock();

        logs = Transformations.switchMap(date, d -> repo.logsByDate(d));
        totalKg = Transformations.switchMap(date, d -> repo.totalKg(d));
        totalCost = Transformations.switchMap(date, d -> repo.totalCost(d));

        String today = DateUtil.today();
        todayKg = repo.totalKg(today);
        todayCost = repo.totalCost(today);
    }

    public void setDate(String d) { date.setValue(d); }

    public String getDate() {
        String d = date.getValue();
        return d == null ? DateUtil.today() : d;
    }
}