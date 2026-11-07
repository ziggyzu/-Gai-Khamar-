package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gaikhamar.app.data.entity.HealthRecord;

import java.util.List;

@Dao
public interface HealthDao {
    @Insert long insert(HealthRecord r);
    @Update void update(HealthRecord r);

    @Query("SELECT * FROM health_records WHERE animalId = :animalId ORDER BY date DESC, id DESC")
    LiveData<List<HealthRecord>> byAnimal(long animalId);

    @Query("SELECT * FROM health_records WHERE animalId = :animalId AND status = 'ONGOING'")
    List<HealthRecord> ongoingSync(long animalId);

    @Query("SELECT COUNT(*) FROM health_records WHERE status = 'ONGOING' "
            + "AND IFNULL(followUpDate, '') != '' AND followUpDate <= :today")
    int followUpDueSync(String today);
}