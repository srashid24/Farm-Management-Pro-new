package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sellers")
public class Seller {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String phone;
    public String address;
    public String company;
    public String email;
    public String supplyType;       // Seeds, Fertilizer, Wrap, Machinery, Fuel, Services, Other
    public double totalSupplied;
    public String notes;
    public long createdAt;

    public Seller(String name, String phone, String address, String company,
                  String email, String supplyType, String notes) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.company = company;
        this.email = email;
        this.supplyType = supplyType;
        this.notes = notes;
        this.totalSupplied = 0;
        this.createdAt = System.currentTimeMillis();
    }
}
