package com.gaikhamar.app.data.entity;

public class ProfitRow {
    public long id;
    public String title;
    public String category;
    public double income;
    public double cost;

    public ProfitRow(long id, String title, String category, double income, double cost) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.income = income;
        this.cost = cost;
    }

    public double profit() { return income - cost; }
}