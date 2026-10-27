package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "milk_records", indices = {@Index("date"), @Index("animalId")})
public class MilkRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long animalId;
    @NonNull public String animalCode = "";
    @NonNull public String date = "";
    @NonNull public String session = "MORNING";  // MORNING, EVENING
    public double liters;
    public long createdAt;
}