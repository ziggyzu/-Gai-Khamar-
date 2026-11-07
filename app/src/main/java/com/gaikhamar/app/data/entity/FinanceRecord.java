package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "finance_records", indices = {@Index("date"), @Index("animalId")})
public class FinanceRecord {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String type = "EXPENSE";     // INCOME, EXPENSE
    @NonNull public String category = "OTHER_EXPENSE";
    public double amount;
    @NonNull public String date = "";
    public String note;
    public long animalId;                        // 0 = farm level
    public long createdAt;
}