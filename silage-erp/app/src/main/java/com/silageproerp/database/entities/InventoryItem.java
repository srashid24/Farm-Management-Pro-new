package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory")
public class InventoryItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String itemName;
    public String category;         // Silage, Wrap, Seeds, Fertilizer, Fuel, Tools, Other
    public double quantity;
    public String unit;             // Tons, Bales, Liters, Kg, Bags, Pieces
    public double unitCost;
    public double totalValue;
    public String storageLocation;
    public String supplierName;
    public String dateAdded;
    public double minimumStock;
    public String notes;
    public String imagePath;        // Path to sample/item photo
    public long createdAt;

    public InventoryItem(String itemName, String category, double quantity, String unit,
                         double unitCost, String storageLocation, String supplierName,
                         String dateAdded, double minimumStock, String notes) {
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.unitCost = unitCost;
        this.totalValue = quantity * unitCost;
        this.storageLocation = storageLocation;
        this.supplierName = supplierName;
        this.dateAdded = dateAdded;
        this.minimumStock = minimumStock;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
