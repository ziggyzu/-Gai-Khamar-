package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"phone"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String name = "";
    @NonNull public String phone = "";
    @NonNull public String farmName = "";
    @NonNull public String farmCode = "GK";
    @NonNull public String passwordHash = "";
    @NonNull public String role = "ADMIN"; // ADMIN, MANAGER, WORKER
    public long createdAt;
}