package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales",
    foreignKeys = {
        @ForeignKey(entity = Buyer.class,
            parentColumns = "id",
            childColumns = "buyerId",
            onDelete = ForeignKey.SET_NULL)
    })
public class Sale {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int buyerId;
    public String buyerName;        // Cached for display
    public String silagType;
    public double quantityTons;
    public int balesCount;
    public double pricePerTon;
    public double pricePerBale;
    public double silageCost;
    public double transportCost;
    public double transportDistanceKm;
    public String departureLocation;
    public String deliveryLocation;
    public double totalAmount;
    public double amountPaid;
    public double balance;
    public String saleDate;
    public String deliveryDate;
    public String deliveryStatus;   // Pending, In Transit, Delivered, Cancelled
    public String paymentStatus;    // Unpaid, Partial, Paid
    public String invoiceNumber;
    public String notes;
    public long createdAt;

    public Sale(int buyerId, String buyerName, String silagType, double quantityTons,
                int balesCount, double pricePerTon, double pricePerBale,
                double transportCost, double transportDistanceKm,
                String departureLocation, String deliveryLocation,
                double amountPaid, String saleDate, String deliveryDate, String notes) {
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.silagType = silagType;
        this.quantityTons = quantityTons;
        this.balesCount = balesCount;
        this.pricePerTon = pricePerTon;
        this.pricePerBale = pricePerBale;
        this.silageCost = (quantityTons * pricePerTon) + (balesCount * pricePerBale);
        this.transportCost = transportCost;
        this.transportDistanceKm = transportDistanceKm;
        this.departureLocation = departureLocation;
        this.deliveryLocation = deliveryLocation;
        this.totalAmount = this.silageCost + transportCost;
        this.amountPaid = amountPaid;
        this.balance = this.totalAmount - amountPaid;
        this.saleDate = saleDate;
        this.deliveryDate = deliveryDate;
        this.deliveryStatus = "Pending";
        this.paymentStatus = amountPaid <= 0 ? "Unpaid" : (amountPaid >= this.totalAmount ? "Paid" : "Partial");
        this.invoiceNumber = "INV-" + System.currentTimeMillis();
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
