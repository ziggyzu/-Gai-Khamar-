package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "health_records", indices = {@Index("animalId")})
public class HealthRecord {
    @PrimaryKey(autoGenerate = true) public long id;
    public long animalId;
    @NonNull public String animalCode = "";
    @NonNull public String date = "";
    @NonNull public String condition = "OTHER";
    public String symptoms;
    public double temperatureC;
    public String treatment;
    public String vetName;
    public double cost;
    public int withdrawalDays;
    public String followUpDate;
    @NonNull public String status = "ONGOING";   // ONGOING, RECOVERED
    public long createdAt;
}