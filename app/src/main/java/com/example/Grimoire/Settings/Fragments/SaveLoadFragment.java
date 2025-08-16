package com.example.Grimoire.Settings.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Grimoire.Backend.AppDatabase;
import com.example.Grimoire.Backend.Page;
import com.example.Grimoire.R;
import com.example.Grimoire.Settings.SettingOption;
import com.example.Grimoire.Settings.SettingsAdapter;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SaveLoadFragment extends Fragment {
    private ActivityResultLauncher<String[]> filePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the file picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        importDatabaseFromUri(uri); // Load JSON from the picked file
                    }
                }
        );

        RecyclerView recyclerView = view.findViewById(R.id.settings_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<SettingOption> options = new ArrayList<>();

        // The four theme options are now located here
        options.add(new SettingOption("Save Data", "Save your data separately to your device", () -> {
            // Show toast immediately on main thread
            if (getContext() != null) {
                Toast.makeText(getContext(), "Downloading Grimoire Data", Toast.LENGTH_SHORT).show();
            }
            exportDatabaseToJson(getContext(), false);
        }));
        options.add(new SettingOption("Load Data", "Load your data separately from your device", () -> {
            if (getContext() == null) return;

            new AlertDialog.Builder(getContext())
                    .setTitle("Warning")
                    .setMessage("This will overwrite all content currently in the app. What would you like to do?")
                    .setPositiveButton("Upload", (dialog, which) -> selectFileAndImport())
                    .setNeutralButton("Download backup then upload", (dialog, which) -> {
                        exportDatabaseToJson(getContext(), true);
                        selectFileAndImport();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }));


        recyclerView.setAdapter(new SettingsAdapter(options));
    }
    private void exportDatabaseToJson(Context context, boolean backup) {
        new Thread(() -> {
            try {
                // 1. Get all data from Room
                AppDatabase db = AppDatabase.getDatabase(context);
                List<Page> allItems = db.pageDao().getAllPages();

                // 2. Convert to JSON
                Gson gson = new Gson();
                String jsonString = gson.toJson(allItems);

                // 3. Save to Downloads
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String baseName = backup ? "Grimoire_Backup_" + date : "Grimoire_" + date;

                File file = getUniqueFile(downloadsDir, baseName, ".json");

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
                fos.close();

                Log.d("Export", "Saved to " + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void selectFileAndImport() {
        // Only allow JSON files
        filePickerLauncher.launch(new String[]{"application/json"});
    }

    private void importDatabaseFromUri(Uri uri) {
        if (getContext() == null) return;

        new Thread(() -> {
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                if (inputStream == null) return;

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                Gson gson = new Gson();
                Page[] pagesArray = gson.fromJson(jsonBuilder.toString(), Page[].class);
                List<Page> pages = Arrays.asList(pagesArray);

                AppDatabase db = AppDatabase.getDatabase(getContext());
                db.pageDao().clearAllPages(); //Clear db
                db.pageDao().insertAll(pages); //import from file

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Import complete!", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to import!", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }
    private File getUniqueFile(File dir, String baseName, String extension) {
        File file = new File(dir, baseName + extension);
        int counter = 1;
        while (file.exists()) {
            file = new File(dir, baseName + " (" + counter + ")" + extension);
            counter++;
        }
        return file;
    }

}