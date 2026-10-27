package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.WeightRecord;

import java.util.List;

@Dao
public interface WeightDao {
    @Insert
    long insert(WeightRecord r);

    @Query("SELECT * FROM weight_records WHERE animalId = :animalId ORDER BY date DESC, id DESC")
    LiveData<List<WeightRecord>> byAnimal(long animalId);

    @Query("SELECT * FROM weight_records WHERE animalId = :animalId ORDER BY date DESC, id DESC LIMIT 1")
    WeightRecord latest(long animalId);
}