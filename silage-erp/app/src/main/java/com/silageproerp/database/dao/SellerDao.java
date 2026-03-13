package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Seller;

import java.util.List;

@Dao
public interface SellerDao {
    @Insert
    long insert(Seller seller);

    @Update
    void update(Seller seller);

    @Delete
    void delete(Seller seller);

    @Query("SELECT * FROM sellers ORDER BY name ASC")
    List<Seller> getAll();

    @Query("SELECT * FROM sellers WHERE id = :id")
    Seller getById(int id);

    @Query("SELECT COUNT(*) FROM sellers")
    int getCount();

    @Query("SELECT * FROM sellers WHERE name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%'")
    List<Seller> search(String query);
}
