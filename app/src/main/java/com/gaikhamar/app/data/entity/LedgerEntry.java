package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "ledger_entries", indices = {@Index("customerId")})
public class LedgerEntry {
    @PrimaryKey(autoGenerate = true) public long id;
    public long customerId;
    @NonNull public String date = "";
    @NonNull public String type = "DELIVERY";    // DELIVERY, PAYMENT
    public double liters;
    public double amount;
    public String note;
    public long createdAt;
}