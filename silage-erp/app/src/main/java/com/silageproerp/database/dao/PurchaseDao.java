package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Purchase;

import java.util.List;

@Dao
public interface PurchaseDao {
    @Insert
    long insert(Purchase purchase);

    @Update
    void update(Purchase purchase);

    @Delete
    void delete(Purchase purchase);

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    List<Purchase> getAll();

    @Query("SELECT * FROM purchases WHERE sellerId = :sellerId")
    List<Purchase> getBySeller(int sellerId);

    @Query("SELECT * FROM purchases WHERE paymentStatus = :status")
    List<Purchase> getByPaymentStatus(String status);

    @Query("SELECT * FROM purchases WHERE id = :id")
    Purchase getById(int id);

    @Query("SELECT SUM(totalCost) FROM purchases")
    double getTotalSpent();

    @Query("SELECT SUM(balance) FROM purchases")
    double getTotalOwed();

    @Query("SELECT COUNT(*) FROM purchases")
    int getCount();
}
