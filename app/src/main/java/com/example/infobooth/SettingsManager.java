package com.example.infobooth;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_THEME_MODE = "theme_mode"; // e.g. "light" or "dark"
    private static final String KEY_ACCENT_COLOR = "accent_color"; // e.g. "#FF0000"
    private static final String KEY_TEXT_COLOR = "text_color"; // e.g. "#000000"
    private static final String KEY_SORT_BY = "sort_by"; // e.g. "alphabetical" or "date"

    private SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Save theme mode
    public void setThemeMode(String mode) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply();
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, "light"); // default light
    }

    // Similar for accent color
    public void setAccentColor(String colorHex) {
        prefs.edit().putString(KEY_ACCENT_COLOR, colorHex).apply();
    }

    public String getAccentColor() {
        return prefs.getString(KEY_ACCENT_COLOR, "#6200EE"); // default purple-ish
    }

    // Text color
    public void setTextColor(String colorHex) {
        prefs.edit().putString(KEY_TEXT_COLOR, colorHex).apply();
    }

    public String getTextColor() {
        return prefs.getString(KEY_TEXT_COLOR, "#000000"); // default black
    }

    // Sort by
    public void setSortBy(String sortBy) {
        prefs.edit().putString(KEY_SORT_BY, sortBy).apply();
    }

    public String getSortBy() {
        return prefs.getString(KEY_SORT_BY, "alphabetical"); // default alphabetical
    }
}
