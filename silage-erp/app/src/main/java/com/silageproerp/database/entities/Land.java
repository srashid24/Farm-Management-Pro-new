package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "lands",
    foreignKeys = @ForeignKey(entity = Farmer.class,
        parentColumns = "id",
        childColumns = "farmerId",
        onDelete = ForeignKey.CASCADE))
public class Land {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int farmerId;
    public String fieldName;
    public double sizeHectares;
    public String location;
    public String gpsCoordinates;
    public String soilType;
    public String cropType;
    public String notes;
    public long createdAt;

    public Land(int farmerId, String fieldName, double sizeHectares, String location,
                String gpsCoordinates, String soilType, String cropType, String notes) {
        this.farmerId = farmerId;
        this.fieldName = fieldName;
        this.sizeHectares = sizeHectares;
        this.location = location;
        this.gpsCoordinates = gpsCoordinates;
        this.soilType = soilType;
        this.cropType = cropType;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
