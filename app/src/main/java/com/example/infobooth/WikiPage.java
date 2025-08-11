package com.example.infobooth;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


public class WikiPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wiki_page);

        // Get the title passed from MainActivity
        String title = getIntent().getStringExtra("item_title");

        //textfield
        EditText editText = findViewById(R.id.editTextDocument);

        // get page using title
        new Thread(() -> {
            // set up database
            AppDatabase db = AppDatabase.getDatabase(this);
            PageDao pageDao = db.pageDao();

            Page page = pageDao.getPageByTitle(getIntent().getStringExtra("item_title"));
            String content = page.getContent();
            //put content into textfield
            runOnUiThread(() -> {
                editText.setText(content);
            });
        }).start();

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //edit button
        FloatingActionButton fab = findViewById(R.id.fab_edit_toggle);

        fab.setOnClickListener(view -> {
            if (!editText.isFocusable()) {
                // Enable editing
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.setClickable(true);
                editText.setCursorVisible(true);
                editText.setLongClickable(true);
                editText.requestFocus();

                fab.setImageResource(android.R.drawable.ic_menu_save); // change icon to save
                //fab.hide()
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

            } else {
                // Save
                savePageContent(editText.getText().toString());

                // Disable editing
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                editText.setCursorVisible(false);

                editText.setClickable(true);
                editText.setLongClickable(true);
                editText.setTextIsSelectable(true);

                fab.setImageResource(android.R.drawable.ic_menu_edit); // change icon back
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
    }

    // Handle back button in toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Saves on close
    @Override
    protected void onPause() {
        super.onPause();
        // Save content if in editing mode or just always save latest content
        EditText editText = findViewById(R.id.editTextDocument);
        savePageContent(editText.getText().toString());
    }

    private void savePageContent(String text) {
        new Thread(() -> {
            // set up database
            AppDatabase db = AppDatabase.getDatabase(this);
            PageDao pageDao = db.pageDao();

            Page page = pageDao.getPageByTitle(getIntent().getStringExtra("item_title"));

            if (page !=  null){
                page.setContent(text);
                pageDao.update(page);
            }
        }).start();
    }

}