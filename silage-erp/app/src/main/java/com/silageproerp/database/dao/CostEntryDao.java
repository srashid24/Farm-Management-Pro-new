package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.CostEntry;

import java.util.List;

@Dao
public interface CostEntryDao {
    @Insert
    long insert(CostEntry entry);

    @Update
    void update(CostEntry entry);

    @Delete
    void delete(CostEntry entry);

    @Query("SELECT * FROM cost_entries ORDER BY date DESC")
    List<CostEntry> getAll();

    @Query("SELECT * FROM cost_entries WHERE category = :category")
    List<CostEntry> getByCategory(String category);

    @Query("SELECT * FROM cost_entries WHERE season = :season")
    List<CostEntry> getBySeason(String season);

    @Query("SELECT * FROM cost_entries WHERE id = :id")
    CostEntry getById(int id);

    @Query("SELECT SUM(amount) FROM cost_entries")
    double getTotalCost();

    @Query("SELECT SUM(amount) FROM cost_entries WHERE season = :season")
    double getCostBySeason(String season);

    @Query("SELECT SUM(amount) FROM cost_entries WHERE category = :category")
    double getCostByCategory(String category);

    @Query("SELECT COUNT(*) FROM cost_entries")
    int getCount();
}
