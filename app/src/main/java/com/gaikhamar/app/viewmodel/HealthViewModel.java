package com.gaikhamar.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.FarmContact;
import com.gaikhamar.app.data.entity.Vaccination;
import com.gaikhamar.app.util.DateUtil;

import java.util.List;

public class HealthViewModel extends AndroidViewModel {
    public final LiveData<List<Vaccination>> vaccinations;
    public final LiveData<Integer> vaccinesDue;
    public final LiveData<List<FarmContact>> contacts;

    public HealthViewModel(@NonNull Application app) {
        super(app);
        Repository repo = Repository.get(app);
        vaccinations = repo.vaccinations();
        vaccinesDue = repo.vaccinesDueCount(DateUtil.plusDays(DateUtil.today(), 7));
        contacts = repo.contacts();
    }
}