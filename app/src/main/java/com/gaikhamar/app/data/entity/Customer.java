package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class Customer {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String name = "";
    public String phone;
    public double ratePerLiter;
    public long createdAt;
}