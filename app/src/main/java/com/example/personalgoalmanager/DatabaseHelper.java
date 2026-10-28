
package com.example.personalgoalmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Goals.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE goals (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "description TEXT, " +
                        "targetDate TEXT, " +
                        "completed INTEGER DEFAULT 0)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Keep existing data when upgrading.
    }

    // CREATE
    public boolean addItem(String name, String description,
                           String targetDate) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("targetDate", targetDate);
        values.put("completed", 0);

        return getWritableDatabase().insert(
                "goals", null, values) != -1;
    }

    // READ
    public Cursor getAllItems() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM goals ORDER BY id DESC",
                null
        );
    }

    // UPDATE item details
    public boolean updateItem(int id, String name,
                              String description, String targetDate) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("targetDate", targetDate);

        return getWritableDatabase().update(
                "goals",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // UPDATE completion status
    public boolean updateStatus(int id, int completed) {
        ContentValues values = new ContentValues();
        values.put("completed", completed);

        return getWritableDatabase().update(
                "goals",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // DELETE
    public boolean deleteItem(int id) {
        return getWritableDatabase().delete(
                "goals",
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }
}
