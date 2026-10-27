package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "breeding_records", indices = {@Index("animalId")})
public class BreedingRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long animalId;
    @NonNull public String type = "HEAT";  // HEAT, INSEMINATION, PREGNANCY_POSITIVE, PREGNANCY_NEGATIVE
    @NonNull public String date = "";
    public String notes;
    public long createdAt;
}