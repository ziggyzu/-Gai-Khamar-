package com.gaikhamar.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.data.entity.MilkRecord;
import com.gaikhamar.app.util.DateUtil;

import java.util.List;

public class MilkViewModel extends AndroidViewModel {

    private final MutableLiveData<String> date = new MutableLiveData<>(DateUtil.today());

    public final LiveData<List<MilkRecord>> records;
    public final LiveData<Double> total;
    public final LiveData<Double> todayLiters;
    public final LiveData<List<Animal>> milkingCows;

    public MilkViewModel(@NonNull Application app) {
        super(app);
        Repository repo = Repository.get(app);
        records = Transformations.switchMap(date, d -> repo.milkByDate(d));
        total = Transformations.switchMap(date, d -> repo.milkTotal(d));
        todayLiters = repo.milkTotal(DateUtil.today());
        milkingCows = repo.milkingCows();
    }

    public void setDate(String d) { date.setValue(d); }

    public String getDate() {
        String d = date.getValue();
        return d == null ? DateUtil.today() : d;
    }
}