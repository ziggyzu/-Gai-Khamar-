package com.gaikhamar.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.gaikhamar.app.data.Repository;
import com.gaikhamar.app.data.entity.Animal;
import com.gaikhamar.app.data.entity.BreedingRecord;
import com.gaikhamar.app.data.entity.WeightRecord;
import com.gaikhamar.app.util.DateUtil;

import java.util.List;

public class AnimalViewModel extends AndroidViewModel {

    private final Repository repo;
    private final MutableLiveData<String[]> filter =
            new MutableLiveData<>(new String[]{"ALL", ""});

    public final LiveData<List<Animal>> animals;
    public final LiveData<Integer> totalCount, beefCount, dairyCount,
            calfCount, sickCount, pregnantCount, calvingSoonCount;

    public AnimalViewModel(@NonNull Application app) {
        super(app);
        repo = Repository.get(app);
        animals = Transformations.switchMap(filter, f -> repo.filter(f[0], f[1]));
        totalCount = repo.countAll();
        beefCount = repo.countByCategory("BEEF");
        dairyCount = repo.countByCategory("DAIRY");
        calfCount = repo.countByCategory("CALF");
        sickCount = repo.countSick();
        pregnantCount = repo.countPregnant();

        String today = DateUtil.today();
        calvingSoonCount = repo.countCalvingBetween(today, DateUtil.plusDays(today, 14));
    }

    public void setCategory(String category) {
        String[] f = filter.getValue();
        filter.setValue(new String[]{category, f == null ? "" : f[1]});
    }

    public void setQuery(String query) {
        String[] f = filter.getValue();
        filter.setValue(new String[]{f == null ? "ALL" : f[0], query});
    }

    public LiveData<Animal> getAnimal(long id) { return repo.getAnimal(id); }
    public LiveData<List<WeightRecord>> weights(long id) { return repo.weights(id); }
    public LiveData<List<BreedingRecord>> breeding(long id) { return repo.breeding(id); }

    /** Total milk of the last 7 days for one cow. */
    public LiveData<Double> milkLast7(long id) {
        String today = DateUtil.today();
        return repo.milkSum(id, DateUtil.plusDays(today, -6), today);
    }
}