package com.example.Grimoire.Settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Grimoire.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RecyclerView recyclerView = findViewById(R.id.settings_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<SettingOption> options = new ArrayList<>();

        options.add(new SettingOption("Sort By", "Change how items are sorted", () -> {
            // Sorting dialog
        }));

        options.add(new SettingOption("Theme Color", "Change the main accent color", () -> {
            // Open a color picker
        }));

        options.add(new SettingOption("Text Color", "Change text display color", () -> {
            // Another dialog
        }));

        options.add(new SettingOption("Theme Mode", "Light or Dark mode", () -> {
            // Show a dialog or toggle directly
        }));

        recyclerView.setAdapter(new SettingsAdapter(options));
    }
}
