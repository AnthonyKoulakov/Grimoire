package com.example.Grimoire.ContentPage;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.Grimoire.Backend.AppDatabase;
import com.example.Grimoire.Backend.Page;
import com.example.Grimoire.Backend.PageDao;
import com.example.Grimoire.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.content.Intent;
import android.widget.FrameLayout;
import android.text.InputType;
import android.widget.TextView;


public class ContentPage extends AppCompatActivity {

    private boolean isEditingTitle = false;
    private Page page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_page);

        // Get the title passed from MainActivity
        String title = getIntent().getStringExtra("item_title");
        EditText titleEdit = findViewById(R.id.titleEditText);
        titleEdit.setText(title);

        //textfield
        TextView textView = findViewById(R.id.textViewContent);
        EditText editText = findViewById(R.id.editTextContent);

        // get page using title
        new Thread(() -> {
            // set up database
            AppDatabase db = AppDatabase.getDatabase(this);
            PageDao pageDao = db.pageDao();

            page = pageDao.getPageByTitle(getIntent().getStringExtra("item_title"));
            String content = page.getContent();
            //put content into textfields
            runOnUiThread(() -> {
                textView.setText(content);
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

            // Enable editing
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);

            fab.hide();
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

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
        EditText editText = findViewById(R.id.editTextContent);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.content_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            EditText titleEditText = findViewById(R.id.titleEditText);

            if(!isEditingTitle){
                isEditingTitle = true;
                // Enable editing for the title
                titleEditText.setFocusable(true);
                titleEditText.setFocusableInTouchMode(true);
                titleEditText.setClickable(true);
                titleEditText.setCursorVisible(true);
                titleEditText.setLongClickable(true);
                titleEditText.requestFocus();
                titleEditText.setSelection(titleEditText.getText().length());

                // Show keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT);

                //Icon changes
                item.setIcon(android.R.drawable.ic_menu_save);


                return true;
            }else{
                isEditingTitle = false;
                // Disable editing for the title
                titleEditText.setFocusable(false);
                titleEditText.setFocusableInTouchMode(false);
                titleEditText.setClickable(false);
                titleEditText.setCursorVisible(false);
                titleEditText.setLongClickable(false);

                // hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);

                //Icon changes
                item.setIcon(android.R.drawable.ic_menu_edit);

                //database update
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getDatabase(this);
                    PageDao pageDao = db.pageDao();

                    page.setTitle(titleEditText.getText().toString());
                    pageDao.update(page);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updated_title", titleEditText.getText().toString());
                    setResult(RESULT_OK, resultIntent);

                }).start();

            }
        }
        return super.onOptionsItemSelected(item);
    }


}