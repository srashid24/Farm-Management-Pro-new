package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Farmer;

import java.util.List;

@Dao
public interface FarmerDao {
    @Insert
    long insert(Farmer farmer);

    @Update
    void update(Farmer farmer);

    @Delete
    void delete(Farmer farmer);

    @Query("SELECT * FROM farmers ORDER BY name ASC")
    List<Farmer> getAll();

    @Query("SELECT * FROM farmers WHERE id = :id")
    Farmer getById(int id);

    @Query("SELECT * FROM farmers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    List<Farmer> search(String query);

    @Query("SELECT COUNT(*) FROM farmers")
    int getCount();
}
