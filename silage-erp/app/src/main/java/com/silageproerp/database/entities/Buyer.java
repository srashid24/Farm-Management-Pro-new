package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "buyers")
public class Buyer {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String phone;
    public String address;
    public String company;
    public String email;
    public String buyerType;        // Individual, Farm, Feedlot, Dairy, Other
    public double totalPurchased;
    public double totalOwed;
    public String notes;
    public long createdAt;

    public Buyer(String name, String phone, String address, String company,
                 String email, String buyerType, String notes) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.company = company;
        this.email = email;
        this.buyerType = buyerType;
        this.notes = notes;
        this.totalPurchased = 0;
        this.totalOwed = 0;
        this.createdAt = System.currentTimeMillis();
    }
}
