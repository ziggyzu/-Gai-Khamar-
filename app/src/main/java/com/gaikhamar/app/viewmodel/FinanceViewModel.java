package com.gaikhamar.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.FinanceRecord;
import com.gaikhamar.app.util.DateUtil;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {
    private final MutableLiveData<String> month = new MutableLiveData<>(DateUtil.monthKey(0));

    public final LiveData<String> monthLive = month;
    public final LiveData<List<FinanceRecord>> records;
    public final LiveData<Double> income;
    public final LiveData<Double> expense;

    public FinanceViewModel(@NonNull Application app) {
        super(app);
        Repository repo = Repository.get(app);
        records = Transformations.switchMap(month, m -> repo.financeBetween(m + "-01", m + "-31"));
        income = Transformations.switchMap(month, m -> repo.financeSum("INCOME", m + "-01", m + "-31"));
        expense = Transformations.switchMap(month, m -> repo.financeSum("EXPENSE", m + "-01", m + "-31"));
    }

    public String getMonth() {
        String m = month.getValue();
        return m == null ? DateUtil.monthKey(0) : m;
    }

    public void shiftMonth(int delta) {
        month.setValue(DateUtil.shiftMonth(getMonth(), delta));
    }
}