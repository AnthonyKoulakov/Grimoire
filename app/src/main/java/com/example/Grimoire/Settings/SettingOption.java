package com.example.Grimoire.Settings;

public class SettingOption {
    private String title;
    private String description;
    private Runnable action; // what happens when clicked

    public SettingOption(String title, String description, Runnable action) {
        this.title = title;
        this.description = description;
        this.action = action;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Runnable getAction() { return action; }
}

