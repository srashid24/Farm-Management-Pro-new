package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "packaging")
public class Packaging {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int harvestId;
    public String harvestSeason;    // Cached
    public String landName;         // Cached
    public int baleCount;
    public double baleWeightKg;
    public double totalWeightTons;
    public String packagingType;    // Round Bale, Square Bale, Pit, Bag
    public String wrapLayers;       // 4, 6, 8 layers
    public String wrapColour;
    public double wrapCostPerBale;
    public double totalWrapCost;
    public String packagingDate;
    public String storageLocation;
    public String contractor;
    public double contractorCost;
    public String notes;
    public long createdAt;

    public Packaging(int harvestId, String harvestSeason, String landName,
                     int baleCount, double baleWeightKg, String packagingType,
                     String wrapLayers, String wrapColour, double wrapCostPerBale,
                     String packagingDate, String storageLocation,
                     String contractor, double contractorCost, String notes) {
        this.harvestId = harvestId;
        this.harvestSeason = harvestSeason;
        this.landName = landName;
        this.baleCount = baleCount;
        this.baleWeightKg = baleWeightKg;
        this.totalWeightTons = (baleCount * baleWeightKg) / 1000.0;
        this.packagingType = packagingType;
        this.wrapLayers = wrapLayers;
        this.wrapColour = wrapColour;
        this.wrapCostPerBale = wrapCostPerBale;
        this.totalWrapCost = baleCount * wrapCostPerBale;
        this.packagingDate = packagingDate;
        this.storageLocation = storageLocation;
        this.contractor = contractor;
        this.contractorCost = contractorCost;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
