package com.example.Grimoire.ContentPage;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.Grimoire.Backend.AppDatabase;
import com.example.Grimoire.Backend.Page;
import com.example.Grimoire.Backend.PageDao;
import com.example.Grimoire.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
                runOnUiThread(() -> renderMarkdownLinks(textView, content));
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
            editText.requestFocus();

            editText.post(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            });
        });
    }

    // Handle back button in toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Saves on close
    protected void onPause() {
        super.onPause();
        EditText editText = findViewById(R.id.editTextContent);
        savePageContent(editText.getText().toString());

        new Thread(() -> {
            boolean created = createPagesFromLinks(page.getContent());
            if (created) {
                runOnUiThread(() -> {
                    Intent intent = new Intent();
                    intent.putExtra("new_pages_created", true);
                    setResult(RESULT_OK, intent);
                });
            }
        }).start();
    }

    private void savePageContent(String text) {
        new Thread(() -> {
            // set up database
            AppDatabase db = AppDatabase.getDatabase(this);
            PageDao pageDao = db.pageDao();

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

    //title editor
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
                titleEditText.setTextIsSelectable(true);

                titleEditText.requestFocus();
                titleEditText.setSelection(titleEditText.getText().length());

                // Show keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT);

                //Icon changes
                item.setIcon(android.R.drawable.ic_menu_save);


                return true;
            }else{

                //database update
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getDatabase(this);
                    PageDao pageDao = db.pageDao();

                    String newTitle = titleEditText.getText().toString();

                    // Check for duplicate title (case-insensitive)
                    Page existing = pageDao.getPageByTitle(newTitle);

                    if (existing != null && existing.getId() != page.getId()) {
                        // Already exists → show popup on UI thread
                        runOnUiThread(() -> {
                            new AlertDialog.Builder(this)
                                    .setTitle("Duplicate Title")
                                    .setMessage("A page with this title already exists. Please choose another title.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
                    } else {
                        // No duplicate → safe to update
                        page.setTitle(newTitle);
                        pageDao.update(page);

                        runOnUiThread(() -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updated_title", newTitle);
                            setResult(RESULT_OK, resultIntent);

                            isEditingTitle = false;

                            // Disable editing for the title
                            titleEditText.setFocusable(false);
                            titleEditText.setFocusableInTouchMode(false);
                            titleEditText.setClickable(false);
                            titleEditText.setCursorVisible(false);
                            titleEditText.setLongClickable(false);
                            titleEditText.setTextIsSelectable(false);

                            // hide keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);

                            // Icon changes
                            item.setIcon(android.R.drawable.ic_menu_edit);
                        });
                    }
                }).start();


            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderMarkdownLinks(TextView textView, String markdown) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        Pattern pattern = Pattern.compile("\\[(.+?)\\]\\((.+?)\\)");
        Matcher matcher = pattern.matcher(markdown);

        int lastEnd = 0;
        while (matcher.find()) {
            // Append text before the match
            builder.append(markdown, lastEnd, matcher.start());

            String displayText = matcher.group(1);
            String targetTitle = matcher.group(2);

            int start = builder.length();
            builder.append(displayText);
            int end = builder.length();

            // Default clickable span with temporary color
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    openPage(targetTitle);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ContextCompat.getColor(ContentPage.this, R.color.white)); // default while checking
                    ds.setUnderlineText(true);
                }
            };
            builder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Async color check
            new Thread(() -> {
                Page linkedPage = AppDatabase.getDatabase(ContentPage.this)
                        .pageDao()
                        .getPageByTitle(targetTitle);

                boolean hasContent = linkedPage != null && linkedPage.size() > 0;
                int newColor = ContextCompat.getColor(ContentPage.this,
                        hasContent ? R.color.link_has_content : R.color.link_no_content);

                runOnUiThread(() -> {
                    // Remove old clickable span and set new color
                    builder.removeSpan(clickableSpan);
                    builder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            openPage(targetTitle);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(newColor);
                            ds.setUnderlineText(true);
                        }
                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setText(builder);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                });
            }).start();

            lastEnd = matcher.end();
        }

        // Append remaining text
        builder.append(markdown, lastEnd, markdown.length());
        textView.setText(builder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }



    private void openPage(String title) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            PageDao pageDao = db.pageDao();

            // Try to find the page
            Page linkedPage = pageDao.getPageByTitle(title);

            if (linkedPage == null) {
                // Page doesn't exist, create it
                linkedPage = new Page(title);
                pageDao.insert(linkedPage);
                linkedPage = pageDao.getPageByTitle(title);
            }

            Page finalLinkedPage = linkedPage; // for use in UI thread
            runOnUiThread(() -> {
                Intent intent = new Intent(ContentPage.this, ContentPage.class);
                intent.putExtra("item_title", finalLinkedPage.getTitle());
                startActivity(intent);
            });
        }).start();
    }

    private boolean createPagesFromLinks(String content) {
        final boolean[] anyCreated = {false};
        AppDatabase db = AppDatabase.getDatabase(this);
        PageDao pageDao = db.pageDao();

        List<String> links = extractAllLinks(content);
        for (String link : links) {
            Page existing = pageDao.getPageByTitle(link);
            if (existing == null) {
                pageDao.insert(new Page(link));
                anyCreated[0] = true;
            }
        }
        return anyCreated[0];
    }

    private List<String> extractAllLinks(String content) {
        List<String> links = new ArrayList<String>();

        Pattern pattern = Pattern.compile("\\[(.+?)\\]\\((.+?)\\)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String target = matcher.group(2);
            links.add(target);
        }

        return links;
    }


}