package com.gaikhamar.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "loans")
public class Loan {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String lender = "";
    public double principal;
    public double balance;
    public double installment;
    @NonNull public String nextDueDate = "";
    public long createdAt;
}
