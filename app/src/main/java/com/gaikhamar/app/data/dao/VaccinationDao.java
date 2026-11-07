package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gaikhamar.app.data.entity.Vaccination;

import java.util.List;

@Dao
public interface VaccinationDao {
    @Insert void insertAll(List<Vaccination> list);
    @Insert long insert(Vaccination v);
    @Update void update(Vaccination v);

    @Query("SELECT * FROM vaccinations ORDER BY nextDueDate, name")
    LiveData<List<Vaccination>> getAll();

    @Query("SELECT * FROM vaccinations WHERE id = :id")
    Vaccination findById(long id);

    @Query("SELECT COUNT(*) FROM vaccinations WHERE nextDueDate != '' AND nextDueDate <= :until")
    LiveData<Integer> dueCount(String until);

    @Query("SELECT COUNT(*) FROM vaccinations WHERE nextDueDate != '' AND nextDueDate <= :until")
    int dueCountSync(String until);
}