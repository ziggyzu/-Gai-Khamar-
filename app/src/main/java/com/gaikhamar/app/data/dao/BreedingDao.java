package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.BreedingRecord;

import java.util.List;

@Dao
public interface BreedingDao {
    @Insert
    long insert(BreedingRecord r);

    @Query("SELECT * FROM breeding_records WHERE animalId = :animalId ORDER BY date DESC, id DESC")
    LiveData<List<BreedingRecord>> byAnimal(long animalId);
}