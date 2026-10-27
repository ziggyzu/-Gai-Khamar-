package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.MilkRecord;

import java.util.List;

@Dao
public interface MilkDao {
    @Insert
    long insert(MilkRecord r);

    @Query("SELECT * FROM milk_records WHERE date = :date ORDER BY id DESC")
    LiveData<List<MilkRecord>> byDate(String date);

    @Query("SELECT IFNULL(SUM(liters), 0) FROM milk_records WHERE date = :date")
    LiveData<Double> total(String date);

    @Query("SELECT IFNULL(SUM(liters), 0) FROM milk_records "
            + "WHERE animalId = :animalId AND date BETWEEN :from AND :to")
    LiveData<Double> sumRange(long animalId, String from, String to);
}