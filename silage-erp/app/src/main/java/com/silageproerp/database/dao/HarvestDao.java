package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Harvest;

import java.util.List;

@Dao
public interface HarvestDao {
    @Insert
    long insert(Harvest harvest);

    @Update
    void update(Harvest harvest);

    @Delete
    void delete(Harvest harvest);

    @Query("SELECT * FROM harvests ORDER BY harvestDate DESC")
    List<Harvest> getAll();

    @Query("SELECT * FROM harvests WHERE landId = :landId")
    List<Harvest> getByLand(int landId);

    @Query("SELECT * FROM harvests WHERE season = :season")
    List<Harvest> getBySeason(String season);

    @Query("SELECT * FROM harvests WHERE id = :id")
    Harvest getById(int id);

    @Query("SELECT SUM(yieldTons) FROM harvests")
    double getTotalYield();

    @Query("SELECT SUM(yieldTons) FROM harvests WHERE season = :season")
    double getYieldBySeason(String season);

    @Query("SELECT SUM(baleCount) FROM harvests")
    int getTotalBales();

    @Query("SELECT COUNT(*) FROM harvests")
    int getCount();
}
