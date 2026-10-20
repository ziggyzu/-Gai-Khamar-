package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gaikhamar.app.data.entity.Animal;

import java.util.List;

@Dao
public interface AnimalDao {
    @Insert
    long insert(Animal animal);

    @Update
    void update(Animal animal);

    @Query("SELECT * FROM animals WHERE status = 'ACTIVE' "
            + "AND (:cat = 'ALL' OR category = :cat) "
            + "AND (farmCattleId LIKE '%' || :q || '%' "
            + "OR IFNULL(earTag,'') LIKE '%' || :q || '%' "
            + "OR IFNULL(name,'') LIKE '%' || :q || '%') "
            + "ORDER BY id DESC")
    LiveData<List<Animal>> filter(String cat, String q);

    @Query("SELECT * FROM animals WHERE id = :id")
    LiveData<Animal> getById(long id);

    @Query("SELECT COUNT(*) FROM animals WHERE status = 'ACTIVE'")
    LiveData<Integer> countAll();

    @Query("SELECT COUNT(*) FROM animals WHERE status = 'ACTIVE' AND category = :cat")
    LiveData<Integer> countByCategory(String cat);

    @Query("SELECT COUNT(*) FROM animals WHERE status = 'ACTIVE' AND healthStatus != 'HEALTHY'")
    LiveData<Integer> countSick();

    @Query("SELECT COUNT(*) FROM animals WHERE status = 'ACTIVE' AND dairyStatus = :s")
    LiveData<Integer> countByDairyStatus(String s);
}