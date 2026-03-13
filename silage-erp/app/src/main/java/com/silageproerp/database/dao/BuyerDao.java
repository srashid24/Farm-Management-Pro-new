package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Buyer;

import java.util.List;

@Dao
public interface BuyerDao {
    @Insert
    long insert(Buyer buyer);

    @Update
    void update(Buyer buyer);

    @Delete
    void delete(Buyer buyer);

    @Query("SELECT * FROM buyers ORDER BY name ASC")
    List<Buyer> getAll();

    @Query("SELECT * FROM buyers WHERE id = :id")
    Buyer getById(int id);

    @Query("SELECT SUM(totalOwed) FROM buyers")
    double getTotalOutstanding();

    @Query("SELECT COUNT(*) FROM buyers")
    int getCount();

    @Query("SELECT * FROM buyers WHERE name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%'")
    List<Buyer> search(String query);
}
