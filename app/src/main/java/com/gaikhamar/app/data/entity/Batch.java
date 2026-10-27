package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "batches")
public class Batch {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull public String name = "";
    public String startDate;
    public String targetSellDate;
    public long createdAt;
}