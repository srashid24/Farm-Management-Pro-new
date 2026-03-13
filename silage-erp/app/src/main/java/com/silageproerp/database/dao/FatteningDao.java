package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Fattening;

import java.util.List;

@Dao
public interface FatteningDao {
    @Insert
    long insert(Fattening fattening);

    @Update
    void update(Fattening fattening);

    @Delete
    void delete(Fattening fattening);

    @Query("SELECT * FROM fattening ORDER BY startDate DESC")
    List<Fattening> getAll();

    @Query("SELECT * FROM fattening WHERE status = 'Active'")
    List<Fattening> getActive();

    @Query("SELECT * FROM fattening WHERE cattleId = :cattleId")
    List<Fattening> getByCattle(int cattleId);

    @Query("SELECT * FROM fattening WHERE id = :id")
    Fattening getById(int id);

    @Query("SELECT COUNT(*) FROM fattening WHERE status = 'Active'")
    int getActiveCount();

    @Query("SELECT SUM(totalFeedCostToDate) FROM fattening WHERE status = 'Active'")
    double getTotalFeedCost();
}
