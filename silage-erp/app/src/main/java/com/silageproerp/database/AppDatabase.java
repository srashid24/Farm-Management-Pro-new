package com.silageproerp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.silageproerp.database.dao.*;
import com.silageproerp.database.entities.*;

@Database(entities = {
        Farmer.class,
        Land.class,
        Harvest.class,
        InventoryItem.class,
        Buyer.class,
        Seller.class,
        Sale.class,
        CostEntry.class,
        Cattle.class,
        Fattening.class,
        Packaging.class,
        Purchase.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract FarmerDao farmerDao();
    public abstract LandDao landDao();
    public abstract HarvestDao harvestDao();
    public abstract InventoryDao inventoryDao();
    public abstract BuyerDao buyerDao();
    public abstract SellerDao sellerDao();
    public abstract SaleDao saleDao();
    public abstract CostEntryDao costEntryDao();
    public abstract CattleDao cattleDao();
    public abstract FatteningDao fatteningDao();
    public abstract PackagingDao packagingDao();
    public abstract PurchaseDao purchaseDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "silage_erp_db"
            )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build();
        }
        return instance;
    }
}
