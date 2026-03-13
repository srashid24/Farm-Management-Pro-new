package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cost_entries")
public class CostEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String category;         // Seeds, Fertilizer, Labour, Machinery, Fuel, Wrap, Transport, Other
    public String description;
    public double amount;
    public double quantity;
    public String unit;
    public double unitCost;
    public String season;
    public String date;
    public String referenceId;      // Link to harvest or sale ID
    public String referenceType;    // harvest, sale, general
    public String supplierName;
    public String notes;
    public long createdAt;

    public CostEntry(String category, String description, double quantity, String unit,
                     double unitCost, String season, String date,
                     String referenceId, String referenceType, String supplierName, String notes) {
        this.category = category;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitCost = unitCost;
        this.amount = quantity * unitCost;
        this.season = season;
        this.date = date;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.supplierName = supplierName;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
