package com.example.Grimoire.Settings.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Grimoire.R;
import com.example.Grimoire.Settings.SettingOption;
import com.example.Grimoire.Settings.SettingsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ThemesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.settings_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<SettingOption> options = new ArrayList<>();

        // The four theme options are now located here
        options.add(new SettingOption("Theme Color", "Change the main accent color", () -> {
            // Open a color picker dialog
        }));
        options.add(new SettingOption("Text Color", "Change text display color", () -> {
            // Open a text color picker dialog
        }));
        options.add(new SettingOption("Theme Mode", "Light or Dark mode", () -> {
            // Toggle light/dark mode
        }));
        options.add(new SettingOption("Icon Style", "Change icon appearance", () -> {
            // Open an icon style selector
        }));

        // To add more theme-related options, add them here

        recyclerView.setAdapter(new SettingsAdapter(options));
    }
}