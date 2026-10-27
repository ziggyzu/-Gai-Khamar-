package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.FeedLog;

import java.util.List;

@Dao
public interface FeedLogDao {
    @Insert
    long insert(FeedLog log);

    @Query("SELECT * FROM feed_logs WHERE date = :date ORDER BY id DESC")
    LiveData<List<FeedLog>> getByDate(String date);

    @Query("SELECT IFNULL(SUM(quantityKg), 0) FROM feed_logs WHERE date = :date")
    LiveData<Double> totalKg(String date);

    @Query("SELECT IFNULL(SUM(totalCost), 0) FROM feed_logs WHERE date = :date")
    LiveData<Double> totalCost(String date);
}