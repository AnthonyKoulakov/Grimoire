package com.example.Grimoire.Settings;

public class SettingOption {
    private String title;
    private String description;
    private Runnable clickAction;

    public SettingOption(String title, String description, Runnable clickAction) {
        this.title = title;
        this.description = description;
        this.clickAction = clickAction;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Runnable getClickAction() {
        return clickAction;
    }
}