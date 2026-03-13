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
    public String blockId;          // Field/block identifier e.g. "BLK-001"
    public String fieldName;
    public double sizeHectares;
    public double sizeAcres;        // Auto-calculated
    public String location;
    public String gpsCoordinates;
    public String soilType;
    public String cropType;

    // Land contract details
    public String contractType;     // Owned, Leased, Rented, Communal
    public String landownerName;
    public double contractCost;     // Rent/lease per season
    public String contractStartDate;
    public String contractEndDate;
    public String contractNotes;

    public String notes;
    public long createdAt;

    public Land(int farmerId, String blockId, String fieldName, double sizeHectares,
                String location, String gpsCoordinates, String soilType, String cropType,
                String contractType, String landownerName, double contractCost,
                String contractStartDate, String contractEndDate, String notes) {
        this.farmerId = farmerId;
        this.blockId = blockId;
        this.fieldName = fieldName;
        this.sizeHectares = sizeHectares;
        this.sizeAcres = sizeHectares * 2.47105;
        this.location = location;
        this.gpsCoordinates = gpsCoordinates;
        this.soilType = soilType;
        this.cropType = cropType;
        this.contractType = contractType;
        this.landownerName = landownerName;
        this.contractCost = contractCost;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}
