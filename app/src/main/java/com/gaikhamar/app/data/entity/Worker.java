package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workers")
public class Worker {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String name = "";
    public String phone;
    @NonNull public String role = "WORKER";   // WORKER, MANAGER
    public double dailyWage;
    public long createdAt;
}
