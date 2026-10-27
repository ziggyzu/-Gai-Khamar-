package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "feed_types")
public class FeedType {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String name = "";
    public double costPerKg;
    public double stockKg;
    public double lowStockLevelKg;
    public long createdAt;
}