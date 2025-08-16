package com.example.Grimoire.Backend;

import androidx.annotation.NonNull;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "pages")
public class Page {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String content;

    // Full constructor (used by Room)
    public Page(int id, @NonNull String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
    @Ignore
    public Page(String title, String content) {
        this.title = title;
        this.content = content;
    }
    @Ignore
    public Page(String title) {
        this.title = title;
        content = "";
    }

    public void setTitle(String title) { this.title = title;}

    public void setContent(String content){
        this.content = content;
    }

    public String getTitle(){
        return title;
    }

    public String getContent(){
        return content;
    }

    public int getId(){return id;}

    public int size(){return content.length();}
}
