package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.InventoryItem;

import java.util.List;

@Dao
public interface InventoryDao {
    @Insert
    long insert(InventoryItem item);

    @Update
    void update(InventoryItem item);

    @Delete
    void delete(InventoryItem item);

    @Query("SELECT * FROM inventory ORDER BY itemName ASC")
    List<InventoryItem> getAll();

    @Query("SELECT * FROM inventory WHERE category = :category")
    List<InventoryItem> getByCategory(String category);

    @Query("SELECT * FROM inventory WHERE id = :id")
    InventoryItem getById(int id);

    @Query("SELECT * FROM inventory WHERE quantity <= minimumStock")
    List<InventoryItem> getLowStock();

    @Query("SELECT SUM(totalValue) FROM inventory")
    double getTotalValue();

    @Query("SELECT COUNT(*) FROM inventory")
    int getCount();

    @Query("SELECT * FROM inventory WHERE itemName LIKE '%' || :query || '%'")
    List<InventoryItem> search(String query);
}
