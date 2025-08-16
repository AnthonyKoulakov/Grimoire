package com.example.Grimoire.Settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.Grimoire.R;
import com.example.Grimoire.Settings.Fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .commit();
        }
    }
}