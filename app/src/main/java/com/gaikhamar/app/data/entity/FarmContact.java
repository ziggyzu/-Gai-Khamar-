package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "farm_contacts")
public class FarmContact {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String name = "";
    @NonNull public String phone = "";
    @NonNull public String role = "OTHER";   // VET, BUYER, SUPPLIER, OTHER
    public long createdAt;
}