package com.gaikhamar.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.gaikhamar.app.data.entity.CalvingRecord;

@Dao
public interface CalvingDao {
    @Insert
    long insert(CalvingRecord r);
}