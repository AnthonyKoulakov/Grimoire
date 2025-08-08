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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> itemList;
    private List<String> filteredList;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        itemList = new ArrayList<>();
        filteredList = new ArrayList<>(itemList);

        adapter = new MyAdapter(this, filteredList, new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                // Handle item click here
                Intent intent = new Intent(MainActivity.this, WikiPage.class);
                intent.putExtra("item_title", item); // pass the item title
                startActivity(intent);
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
                    itemList.add(itemName); // add to full list
                    filterList("");         // refresh filtered list (optional: you can pass last query)
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

    private void filterList(String query) {
        filteredList.clear();
        for (String item : itemList) {
            if (item.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
