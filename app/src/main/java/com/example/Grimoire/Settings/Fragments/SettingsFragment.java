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

public class SettingsFragment extends Fragment {

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

        // Add regular settings options
        options.add(new SettingOption("Sort By", "Change how items are sorted", () -> {
            // Logic for "Sort By"
        }));

        // Add a "Themes" folder that navigates to the ThemesFragment
        options.add(new SettingOption("Themes", "Manage app themes and colors", () -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ThemesFragment())
                    .addToBackStack(null)
                    .commit();
        }));

        options.add(new SettingOption("Save & Load", "Download and upload app pages", () -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SaveLoadFragment())
                    .addToBackStack(null)
                    .commit();
        }));

        // Add more regular settings here...

        recyclerView.setAdapter(new SettingsAdapter(options));
    }
}