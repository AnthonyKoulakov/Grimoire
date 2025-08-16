package com.example.Grimoire.MainScreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Grimoire.Backend.AppDatabase;
import com.example.Grimoire.Backend.Page;
import com.example.Grimoire.Backend.PageDao;
import com.example.Grimoire.Settings.SettingsActivity;
import com.example.Grimoire.ContentPage.ContentPage;
import com.example.Grimoire.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private List<Page> itemList;
    private List<Page> filteredList;
    private MyAdapter adapter;
    private ActivityResultLauncher<Intent> contentPageLauncher;

    private String oldTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up database
        AppDatabase db = AppDatabase.getDatabase(this);
        PageDao pageDao = db.pageDao();

        // Load saved pages from DB
        new Thread(() -> {
            List<Page> pages = pageDao.getAllPages(); // Get all pages from DB

            runOnUiThread(() -> {
                itemList.clear();
                itemList.addAll(pages);

                filteredList.clear();
                filteredList.addAll(pages);

                // Sort alphabetically on initial load too
                Collections.sort(filteredList, new Comparator<Page>() {
                    @Override
                    public int compare(Page p1, Page p2) {
                        return p1.getTitle().compareToIgnoreCase(p2.getTitle());
                    }
                });

                adapter.notifyDataSetChanged(); // Refresh list on UI
            });
        }).start();

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        itemList = new ArrayList<>();
        filteredList = new ArrayList<>(itemList);

        //Updating page info
        contentPageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String updatedTitle = result.getData().getStringExtra("updated_title");
                        updateTitleInList(updatedTitle);
                    }
                });

        adapter = new MyAdapter(this, filteredList, new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Page item) {
                // Handle item click here
                Intent intent = new Intent(MainActivity.this, ContentPage.class);
                oldTitle = item.getTitle();
                intent.putExtra("item_title", item.getTitle());
                contentPageLauncher.launch(intent);

            }
            @Override
            public void onSelectionChanged(int selectedCount) {
                // Optional: show UI changes (like a delete button) based on selection
                invalidateOptionsMenu(); // triggers onPrepareOptionsMenu
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //add page button
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Add New Item");

            final EditText input = new EditText(MainActivity.this);
            input.setHint("Enter name");
            builder.setView(input);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String itemName = input.getText().toString().trim();
                if (!itemName.isEmpty()) {
                    new Thread(() -> {
                        // Check if the page already exists in the database
                        Page existingPage = pageDao.getPageByTitle(itemName);
                        if (existingPage == null) {
                            // Page doesn't exist, so we can add it
                            Page newPage = new Page(itemName);
                            pageDao.insert(newPage);

                            runOnUiThread(() -> {
                                itemList.add(newPage);
                                filterList("");
                            });
                        } else {
                            // Page already exists, show an error message
                            runOnUiThread(() -> {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Error")
                                        .setMessage("A page with this name already exists.")
                                        .setPositiveButton("OK", null)
                                        .show();
                            });
                        }
                    }).start();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();

            // Use a Handler to post a delayed action to show the keyboard.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    input.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 200); // 200ms delay
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchbar, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Info Booth");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // close keyboard
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        MenuItem settingsItem = menu.findItem(R.id.action_settings);

        boolean selectionMode = adapter.isSelectionMode();

        if (deleteItem != null) {
            deleteItem.setVisible(selectionMode);
        }
        if (settingsItem != null) {
            settingsItem.setVisible(!selectionMode);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    private void filterList(String query) {
        filteredList.clear();
        for (Page page : itemList) {
            if (page.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(page);
            }
        }
        // Sort alphabetically by title, case-insensitive
        Collections.sort(filteredList, new Comparator<Page>() {
            @Override
            public int compare(Page p1, Page p2) {
                return p1.getTitle().compareToIgnoreCase(p2.getTitle());
            }
        });

        adapter.notifyDataSetChanged();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.action_delete){
            new AlertDialog.Builder(this)
                    .setTitle("Delete selected pages?")
                    .setMessage("Are you sure you want to delete the selected pages?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Set<Page> selected = adapter.getSelectedItems();

                        //Del from db
                        new Thread(() -> {
                            AppDatabase db = AppDatabase.getDatabase(this);
                            PageDao pageDao = db.pageDao();
                            for (Page page : selected) {
                                pageDao.delete(page);
                            }

                            runOnUiThread(() -> {
                                itemList.removeAll(selected);
                                adapter.exitSelectionMode();
                                filterList(""); // refresh list
                            });
                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        if (id == R.id.action_settings){
            // open settings menu
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if (adapter.isSelectionMode()) {
            adapter.exitSelectionMode();
        } else {
            super.onBackPressed();
        }
    }

    private void updateTitleInList(String updatedTitle) {
        if (!oldTitle.equals(updatedTitle)){
            for (Page page : filteredList) {
                if (page.getTitle().equals(oldTitle)) {
                    page.setTitle(updatedTitle);
                    break;
                }
            }
        }
        filterList("");  // refreshes filteredList and notifies adapter
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (itemList == null) return; // safety check

        new Thread(() -> {
            List<Page> allPages = AppDatabase.getDatabase(this).pageDao().getAllPages();

            runOnUiThread(() -> {
                itemList.clear();
                itemList.addAll(allPages);
                filterList(""); // rebuilds filteredList and notifies adapter
            });
        }).start();
    }




}
