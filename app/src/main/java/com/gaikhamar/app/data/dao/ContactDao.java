package com.gaikhamar.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaikhamar.app.data.entity.FarmContact;

import java.util.List;

@Dao
public interface ContactDao {
    @Insert long insert(FarmContact c);
    @Delete void delete(FarmContact c);

    @Query("SELECT * FROM farm_contacts ORDER BY name")
    LiveData<List<FarmContact>> getAll();
}