package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Packaging;

import java.util.List;

@Dao
public interface PackagingDao {
    @Insert
    long insert(Packaging packaging);

    @Update
    void update(Packaging packaging);

    @Delete
    void delete(Packaging packaging);

    @Query("SELECT * FROM packaging ORDER BY packagingDate DESC")
    List<Packaging> getAll();

    @Query("SELECT * FROM packaging WHERE harvestId = :harvestId")
    List<Packaging> getByHarvest(int harvestId);

    @Query("SELECT * FROM packaging WHERE id = :id")
    Packaging getById(int id);

    @Query("SELECT SUM(baleCount) FROM packaging")
    int getTotalBales();

    @Query("SELECT SUM(totalWeightTons) FROM packaging")
    double getTotalWeight();

    @Query("SELECT SUM(totalWrapCost + contractorCost) FROM packaging")
    double getTotalPackagingCost();

    @Query("SELECT COUNT(*) FROM packaging")
    int getCount();
}
