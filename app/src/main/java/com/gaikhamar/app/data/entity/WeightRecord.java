package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "weight_records", indices = {@Index("animalId")})
public class WeightRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long animalId;
    @NonNull public String date = "";
    public double weightKg;
    public long createdAt;
}