package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvests",
    foreignKeys = @ForeignKey(entity = Land.class,
        parentColumns = "id",
        childColumns = "landId",
        onDelete = ForeignKey.CASCADE))
public class Harvest {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int landId;
    public String season;
    public String harvestDate;
    public double yieldTons;
    public double moisturePercent;
    public String quality;          // Excellent, Good, Fair, Poor
    public String silagType;        // Maize, Grass, Sorghum, Mixed
    public int baleCount;
    public double baleWeightKg;
    public String storageLocation;
    public double inputCostTotal;
    public String notes;
    public long createdAt;

    public Harvest(int landId, String season, String harvestDate, double yieldTons,
                   double moisturePercent, String quality, String silagType,
                   int baleCount, double baleWeightKg, String storageLocation,
                   double inputCostTotal, String notes) {
        this.landId = landId;
        this.season = season;
        this.harvestDate = harvestDate;
        this.yieldTons = yieldTons;
        this.moisturePercent = moisturePercent;
        this.quality = quality;
        this.silagType = silagType;
        this.baleCount = baleCount;
        this.baleWeightKg = baleWeightKg;
        this.storageLocation = storageLocation;
        this.inputCostTotal = inputCostTotal;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
