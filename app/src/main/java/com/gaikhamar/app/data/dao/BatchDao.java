package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.Batch;
import com.gaikhamar.app.data.entity.BatchSummary;

import java.util.List;

@Dao
public interface BatchDao {
    @Insert
    long insert(Batch b);

    @Query("SELECT * FROM batches ORDER BY name")
    LiveData<List<Batch>> getAll();

    @Query("SELECT b.id AS id, b.name AS name, b.targetSellDate AS targetSellDate, "
            + "COUNT(a.id) AS animalCount, "
            + "IFNULL(SUM(a.purchasePrice), 0) AS totalCost, "
            + "IFNULL(SUM(a.currentWeightKg * a.expectedPricePerKg), 0) AS expectedValue "
            + "FROM batches b LEFT JOIN animals a ON a.batchId = b.id AND a.status = 'ACTIVE' "
            + "GROUP BY b.id ORDER BY b.id DESC")
    LiveData<List<BatchSummary>> summaries();
}