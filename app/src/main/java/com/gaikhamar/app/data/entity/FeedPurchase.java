package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "feed_purchases")
public class FeedPurchase {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long feedTypeId;
    public double quantityKg;
    public double pricePerKg;
    public double totalCost;
    @NonNull public String date = "";
    public long createdAt;
}