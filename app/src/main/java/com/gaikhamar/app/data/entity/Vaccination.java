package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vaccinations")
public class Vaccination {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String name = "";
    public int intervalDays;
    @NonNull public String lastDate = "";
    @NonNull public String nextDueDate = "";
    public long createdAt;
}