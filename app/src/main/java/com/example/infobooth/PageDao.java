package com.example.infobooth;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface PageDao {
    @Insert
    void insert(Page page);

    @Update
    void update(Page page);

    @Delete
    void delete(Page page);

    @Query("SELECT * FROM pages")
    List<Page> getAllPages();

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    Page getPageById(int id);

    @Query("SELECT * FROM pages WHERE title = :title LIMIT 1")
    Page getPageByTitle(String title);
}

