package com.example.tovisit_prakash_c0773839_android.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface PlaceDao {


    @Query("SELECT * FROM place")
    Maybe<List<Place>> getAll();

    @Insert
    void insertAll(Place... folders);

    @Update
    void update(Place folder);

    @Delete
    void delete(Place folder);
}
