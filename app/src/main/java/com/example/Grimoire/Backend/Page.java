package com.example.Grimoire.Backend;

import android.util.Log;

import androidx.annotation.NonNull;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "pages")
public class Page {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String content;
    public List<String> tags = new ArrayList<>();

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

    public void addTag(String tag){tags.add(tag);}

    public void setTags(List<String> tags){
        Log.d("IN", tags.toString());
        this.tags = tags;}

    public List<String> getTags(){
        Log.d("OUT", tags.toString());
        return tags;}

    public int size(){return content.length();}

}
