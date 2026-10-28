package com.example.personalgoalmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText etName, etDescription, etDate, etSearch;
    private Button btnSave, btnClear, btnSearch;
    private ImageButton btnSearchIcon;
    private LinearLayout layoutSearch;
    private LinearLayout layoutPending, layoutCompleted;

    private DatabaseHelper db;
    private int selectedId = -1;
    private String currentSearch = "";

    private final int purple = Color.rgb(101, 88, 200);
    private final int darkPurple = Color.rgb(55, 49, 107);
    private final int green = Color.rgb(35, 116, 71);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etSearch = findViewById(R.id.etSearch);

        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        btnSearch = findViewById(R.id.btnSearch);
        btnSearchIcon = findViewById(R.id.btnSearchIcon);

        layoutSearch = findViewById(R.id.layoutSearch);
        layoutPending = findViewById(R.id.layoutPending);
        layoutCompleted = findViewById(R.id.layoutCompleted);

        db = new DatabaseHelper(this);

        btnSave.setOnClickListener(v -> saveItem());
        btnClear.setOnClickListener(v -> clearForm());

        // Show or hide search when icon is tapped
        btnSearchIcon.setOnClickListener(v -> {
            if (layoutSearch.getVisibility() == View.GONE) {
                layoutSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();

                InputMethodManager imm =
                        (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(
                            etSearch,
                            InputMethodManager.SHOW_IMPLICIT);
                }
            } else {
                layoutSearch.setVisibility(View.GONE);
                etSearch.setText("");
                currentSearch = "";
                loadItems();
            }
        });

        // Search by item name or description
        btnSearch.setOnClickListener(v -> {
            currentSearch = etSearch.getText()
                    .toString().trim().toLowerCase();
            loadItems();
        });

        loadItems();
    }

    // CREATE and UPDATE
    private void saveItem() {
        String name = etName.getText().toString().trim();
        String description =
                etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Enter a bucket list item");
            etName.requestFocus();
            return;
        }

        boolean editing = selectedId != -1;
        boolean success;

        if (editing) {
            success = db.updateItem(
                    selectedId, name, description, date);
        } else {
            success = db.addItem(name, description, date);
        }

        if (success) {
            Toast.makeText(this,
                    editing ? "Bucket list updated!"
                            : "Added to bucket list!",
                    Toast.LENGTH_SHORT).show();

            clearForm();
            loadItems();
        } else {
            Toast.makeText(this, "Operation failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // READ and SEARCH
    private void loadItems() {
        layoutPending.removeAllViews();
        layoutCompleted.removeAllViews();

        int pendingCount = 0;
        int completedCount = 0;

        Cursor cursor = db.getAllItems();

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id"));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name"));

                String description = cursor.getString(
                        cursor.getColumnIndexOrThrow("description"));

                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow("targetDate"));

                int completed = cursor.getInt(
                        cursor.getColumnIndexOrThrow("completed"));

                String searchable =
                        (name + " " + description + " " + date)
                                .toLowerCase();

                if (!currentSearch.isEmpty()
                        && !searchable.contains(currentSearch)) {
                    continue;
                }

                if (completed == 1) {
                    completedCount++;
                } else {
                    pendingCount++;
                }

                addItemCard(id, name, description, date, completed);
            }
        } finally {
            cursor.close();
        }

        if (pendingCount == 0) {
            addEmptyMessage(layoutPending,
                    currentSearch.isEmpty()
                            ? "No pending items. Add a new dream!"
                            : "No matching pending items.");
        }

        if (completedCount == 0) {
            addEmptyMessage(layoutCompleted,
                    currentSearch.isEmpty()
                            ? "Your completed items will appear here."
                            : "No matching completed items.");
        }
    }

    // Create a card without a separate status label
    private void addItemCard(int id, String name,
                             String description, String date,
                             int completed) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1),
                completed == 1
                        ? Color.rgb(195, 226, 203)
                        : Color.rgb(224, 220, 245));

        card.setBackground(background);
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        cardParams.bottomMargin = dp(12);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(completed == 1 ? green : darkPurple);
        card.addView(title);

        if (!description.isEmpty()) {
            TextView detail = new TextView(this);
            detail.setText(description);
            detail.setTextSize(14);
            detail.setTextColor(Color.DKGRAY);
            detail.setPadding(0, dp(5), 0, dp(5));
            card.addView(detail);
        }

        if (!date.isEmpty()) {
            TextView dateText = new TextView(this);
            dateText.setText("Target date: " + date);
            dateText.setTextSize(13);
            dateText.setTextColor(Color.GRAY);
            dateText.setPadding(0, dp(3), 0, dp(6));
            card.addView(dateText);
        }

        // Edit, Delete, and completion buttons
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        Button edit = makeButton("Edit", purple);
        Button delete = makeButton(
                "Delete", Color.rgb(195, 55, 65));

        Button toggle = makeButton(
                completed == 1 ? "Move to Pending" : "Completed",
                completed == 1 ? Color.DKGRAY : green);

        actions.addView(edit, weightedParams());
        actions.addView(delete, weightedParams());
        actions.addView(toggle, weightedParams());

        card.addView(actions);

        if (completed == 1) {
            layoutCompleted.addView(card, cardParams);
        } else {
            layoutPending.addView(card, cardParams);
        }

        // EDIT
        edit.setOnClickListener(v -> {
            selectedId = id;
            etName.setText(name);
            etDescription.setText(description);
            etDate.setText(date);
            btnSave.setText("Update Bucket List");

            etName.requestFocus();
        });

        // DELETE
        delete.setOnClickListener(v -> confirmDelete(id, name));

        // COMPLETE or MOVE BACK TO PENDING
        toggle.setOnClickListener(v -> {
            int newStatus = completed == 1 ? 0 : 1;

            if (db.updateStatus(id, newStatus)) {
                Toast.makeText(this,
                        newStatus == 1
                                ? "Bucket list completed!"
                                : "Moved back to Pending!",
                        Toast.LENGTH_SHORT).show();

                loadItems();
            } else {
                Toast.makeText(this, "Status update failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Button styling
    private Button makeButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(10);
        button.setPadding(dp(2), 0, dp(2), 0);
        button.setMinHeight(dp(40));
        button.setMinimumHeight(dp(40));
        button.setBackgroundTintList(
                ColorStateList.valueOf(color));

        return button;
    }

    private LinearLayout.LayoutParams weightedParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f);

        params.setMargins(dp(2), dp(4), dp(2), dp(2));
        return params;
    }

    // Empty-section message
    private void addEmptyMessage(LinearLayout container,
                                 String message) {
        TextView empty = new TextView(this);
        empty.setText(message);
        empty.setTextSize(14);
        empty.setTextColor(Color.GRAY);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(12), dp(20), dp(12), dp(20));
        container.addView(empty);
    }

    // DELETE confirmation
    private void confirmDelete(int id, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bucket List Item")
                .setMessage("Delete \"" + name + "\"?")
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (db.deleteItem(id)) {
                        if (selectedId == id) {
                            clearForm();
                        }

                        Toast.makeText(this, "Item deleted",
                                Toast.LENGTH_SHORT).show();
                        loadItems();
                    } else {
                        Toast.makeText(this, "Delete failed",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    // CLEAR FORM
    private void clearForm() {
        etName.setText("");
        etDescription.setText("");
        etDate.setText("");
        selectedId = -1;
        btnSave.setText("Add to Bucket List");
        etName.clearFocus();
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (db != null) {
            db.close();
        }
        super.onDestroy();
    }
}
