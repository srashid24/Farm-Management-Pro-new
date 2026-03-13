package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "cattle",
    foreignKeys = @ForeignKey(entity = Farmer.class,
        parentColumns = "id",
        childColumns = "ownerId",
        onDelete = ForeignKey.SET_NULL))
public class Cattle {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int ownerId;
    public String ownerName;        // Cached
    public String tagNumber;
    public String name;
    public String breed;
    public String gender;           // Bull, Cow, Heifer, Steer, Calf
    public String dateOfBirth;
    public double currentWeightKg;
    public String status;           // Active, Sold, Deceased, Fattening
    public String color;
    public String source;           // Born on farm, Purchased
    public double purchasePrice;
    public String purchaseDate;
    public String notes;
    public String imagePath;        // Path to cattle photo
    public long createdAt;

    public Cattle(int ownerId, String ownerName, String tagNumber, String name,
                  String breed, String gender, String dateOfBirth, double currentWeightKg,
                  String status, String color, String source,
                  double purchasePrice, String purchaseDate, String notes) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.tagNumber = tagNumber;
        this.name = name;
        this.breed = breed;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.currentWeightKg = currentWeightKg;
        this.status = status;
        this.color = color;
        this.source = source;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
