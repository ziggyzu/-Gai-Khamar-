package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "attendance", indices = {@Index(value = {"workerId", "date"}, unique = true)})
public class Attendance {
    @PrimaryKey(autoGenerate = true) public long id;
    public long workerId;
    @NonNull public String date = "";
    public int present;   // 1 = present, 0 = absent
}
