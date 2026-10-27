package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "feed_logs", indices = {@Index("date")})
public class FeedLog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String date = "";          // yyyy-MM-dd
    public long feedTypeId;
    @NonNull public String feedName = "";
    @NonNull public String groupCode = "BEEF"; // BEEF, MILKING, DRY, CALF
    public double quantityKg;
    public double costPerKg;
    public double totalCost;
    public long createdAt;
}