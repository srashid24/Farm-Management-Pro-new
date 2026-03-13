package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchases")
public class Purchase {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int sellerId;
    public String sellerName;       // Cached
    public String itemName;
    public String category;         // Seeds, Fertilizer, Wrap, Machinery, Fuel, Services, Other
    public double quantity;
    public String unit;
    public double costPerUnit;
    public double totalCost;
    public String purchaseDate;
    public String invoiceNumber;
    public String paymentStatus;    // Paid, Unpaid, Partial
    public double amountPaid;
    public double balance;
    public String notes;
    public long createdAt;

    public Purchase(int sellerId, String sellerName, String itemName, String category,
                    double quantity, String unit, double costPerUnit,
                    String purchaseDate, String invoiceNumber,
                    double amountPaid, String notes) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.costPerUnit = costPerUnit;
        this.totalCost = quantity * costPerUnit;
        this.purchaseDate = purchaseDate;
        this.invoiceNumber = invoiceNumber;
        this.amountPaid = amountPaid;
        this.balance = this.totalCost - amountPaid;
        this.paymentStatus = amountPaid <= 0 ? "Unpaid" : (amountPaid >= this.totalCost ? "Paid" : "Partial");
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
