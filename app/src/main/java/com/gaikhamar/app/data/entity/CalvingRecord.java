package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "calving_records")
public class CalvingRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long motherId;
    public long calfId;
    @NonNull public String date = "";
    public String calfGender;
    public double birthWeightKg;
    public long createdAt;
}