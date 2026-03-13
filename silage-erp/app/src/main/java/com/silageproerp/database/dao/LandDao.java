package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Land;

import java.util.List;

@Dao
public interface LandDao {
    @Insert
    long insert(Land land);

    @Update
    void update(Land land);

    @Delete
    void delete(Land land);

    @Query("SELECT * FROM lands ORDER BY fieldName ASC")
    List<Land> getAll();

    @Query("SELECT * FROM lands WHERE farmerId = :farmerId")
    List<Land> getByFarmer(int farmerId);

    @Query("SELECT * FROM lands WHERE id = :id")
    Land getById(int id);

    @Query("SELECT SUM(sizeHectares) FROM lands")
    double getTotalHectares();

    @Query("SELECT COUNT(*) FROM lands")
    int getCount();

    @Query("SELECT * FROM lands WHERE fieldName LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    List<Land> search(String query);
}
