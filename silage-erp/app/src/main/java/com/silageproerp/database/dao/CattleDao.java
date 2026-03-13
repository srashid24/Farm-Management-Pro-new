package com.silageproerp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.silageproerp.database.entities.Cattle;

import java.util.List;

@Dao
public interface CattleDao {
    @Insert
    long insert(Cattle cattle);

    @Update
    void update(Cattle cattle);

    @Delete
    void delete(Cattle cattle);

    @Query("SELECT * FROM cattle ORDER BY tagNumber ASC")
    List<Cattle> getAll();

    @Query("SELECT * FROM cattle WHERE ownerId = :ownerId")
    List<Cattle> getByOwner(int ownerId);

    @Query("SELECT * FROM cattle WHERE status = :status")
    List<Cattle> getByStatus(String status);

    @Query("SELECT * FROM cattle WHERE id = :id")
    Cattle getById(int id);

    @Query("SELECT COUNT(*) FROM cattle WHERE status = 'Active'")
    int getActiveCount();

    @Query("SELECT COUNT(*) FROM cattle")
    int getCount();

    @Query("SELECT * FROM cattle WHERE tagNumber LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    List<Cattle> search(String query);
}
