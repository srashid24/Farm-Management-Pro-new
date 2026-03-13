package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Sale;

import java.util.List;

@Dao
public interface SaleDao {
    @Insert
    long insert(Sale sale);

    @Update
    void update(Sale sale);

    @Delete
    void delete(Sale sale);

    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    List<Sale> getAll();

    @Query("SELECT * FROM sales WHERE buyerId = :buyerId")
    List<Sale> getByBuyer(int buyerId);

    @Query("SELECT * FROM sales WHERE deliveryStatus = :status")
    List<Sale> getByDeliveryStatus(String status);

    @Query("SELECT * FROM sales WHERE paymentStatus = :status")
    List<Sale> getByPaymentStatus(String status);

    @Query("SELECT * FROM sales WHERE id = :id")
    Sale getById(int id);

    @Query("SELECT SUM(totalAmount) FROM sales")
    double getTotalRevenue();

    @Query("SELECT SUM(balance) FROM sales")
    double getTotalOutstanding();

    @Query("SELECT SUM(transportCost) FROM sales")
    double getTotalTransportCost();

    @Query("SELECT COUNT(*) FROM sales")
    int getCount();

    @Query("SELECT * FROM sales WHERE buyerName LIKE '%' || :query || '%' OR invoiceNumber LIKE '%' || :query || '%'")
    List<Sale> search(String query);
}
