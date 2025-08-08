package com.example.infobooth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        adapter = new MyAdapter(this, filteredList, new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Page item) {
                // Handle item click here
                Intent intent = new Intent(MainActivity.this, WikiPage.class);
                intent.putExtra("item_title", item.getTitle()); // pass the item title
                startActivity(intent);
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
                    //add page to db
                    new Thread(() -> {
                        Page newPage = new Page(itemName);
                        pageDao.insert(newPage);

                        runOnUiThread(() -> {
                            itemList.add(newPage);
                            filterList("");
                        });
                    }).start();

                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
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

    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if(deleteItem != null){
            deleteItem.setVisible(adapter.isSelectionMode());
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

}
