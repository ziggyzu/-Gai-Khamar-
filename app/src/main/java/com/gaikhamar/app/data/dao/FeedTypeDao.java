package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gaikhamar.app.data.entity.FeedType;

import java.util.List;

@Dao
public interface FeedTypeDao {
    @Insert
    void insertAll(List<FeedType> list);

    @Update
    void update(FeedType type);

    @Query("SELECT * FROM feed_types ORDER BY name")
    LiveData<List<FeedType>> getAll();

    @Query("SELECT * FROM feed_types WHERE id = :id")
    FeedType findById(long id);

    @Query("SELECT COUNT(*) FROM feed_types WHERE stockKg <= lowStockLevelKg")
    LiveData<Integer> countLowStock();
}