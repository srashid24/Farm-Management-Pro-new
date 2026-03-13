package com.silageproerp.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "fattening",
    foreignKeys = @ForeignKey(entity = Cattle.class,
        parentColumns = "id",
        childColumns = "cattleId",
        onDelete = ForeignKey.CASCADE))
public class Fattening {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int cattleId;
    public String cattleTag;        // Cached
    public String cattleName;       // Cached
    public String startDate;
    public String endDate;
    public double startWeightKg;
    public double currentWeightKg;
    public double targetWeightKg;
    public double dailyFeedKg;
    public double totalFeedCostToDate;
    public double dailyGainKg;
    public int daysOnProgram;
    public String feedType;         // Silage, Grain, Mixed, TMR
    public String status;           // Active, Completed, Sold
    public double salePrice;
    public String saleDate;
    public String buyerName;
    public String notes;
    public long createdAt;

    public Fattening(int cattleId, String cattleTag, String cattleName,
                     String startDate, double startWeightKg, double targetWeightKg,
                     double dailyFeedKg, String feedType, String notes) {
        this.cattleId = cattleId;
        this.cattleTag = cattleTag;
        this.cattleName = cattleName;
        this.startDate = startDate;
        this.startWeightKg = startWeightKg;
        this.currentWeightKg = startWeightKg;
        this.targetWeightKg = targetWeightKg;
        this.dailyFeedKg = dailyFeedKg;
        this.totalFeedCostToDate = 0;
        this.dailyGainKg = 0;
        this.daysOnProgram = 0;
        this.feedType = feedType;
        this.status = "Active";
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
