package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "animals", indices = {@Index(value = {"farmCattleId"}, unique = true)})
public class Animal {
    @PrimaryKey(autoGenerate = true)   // AUTOINCREMENT: numbers are never reused
    public long id;

    @NonNull public String systemId = "";       // hidden UUID for database/sync
    @NonNull public String farmCattleId = "";   // readable ID, e.g. KH-B-26-0042
    public String earTag;
    public String name;
    @NonNull public String category = "BEEF";   // BEEF, DAIRY, CALF
    public String breed;
    @NonNull public String gender = "FEMALE";
    public String dateOfBirth;
    public String purchaseDate;
    public double purchasePrice;
    public double currentWeightKg;
    public double targetWeightKg;               // beef only
    public String dairyStatus;                  // HEIFER, MILKING, DRY, PREGNANT
    public String motherId;                     // farmCattleId of mother
    public String fatherId;
    @NonNull public String healthStatus = "HEALTHY";
    @NonNull public String status = "ACTIVE";   // ACTIVE, SOLD, DEAD (archived, never deleted)
    public String notes;
    public long createdAt;
}