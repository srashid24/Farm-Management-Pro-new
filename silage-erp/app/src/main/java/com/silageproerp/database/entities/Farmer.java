package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "farmers")
public class Farmer {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String phone;
    public String address;
    public String nationalId;
    public String email;
    public String notes;
    public long createdAt;

    public Farmer(String name, String phone, String address, String nationalId, String email, String notes) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.nationalId = nationalId;
        this.email = email;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
