package com.app.skte;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SKTE.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";
    private static String DATABASE_PATH = "";
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getPath();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // No need to create tables as we are using a pre-populated database
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if needed
    }
    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();
        if (!dbExist) {
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // Database does not exist yet
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private void copyDatabase() throws IOException {
        InputStream input = context.getAssets().open(DATABASE_NAME);
        OutputStream output = new FileOutputStream(DATABASE_PATH);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        output.flush();
        output.close();
        input.close();
    }
    public Cursor get_activity_boy() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM activity_boy  ORDER BY tuoi DESC", null);
    }
    public Cursor get_activity_girl() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM activity_girl ORDER BY tuoi DESC", null);
    }
    public Cursor get_diet_boy() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM diet_boy ORDER BY tuoi DESC", null);
    }
    public Cursor get_diet_girl() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM diet_girl ORDER BY tuoi DESC", null);
    }
    public Cursor get_energy_boy() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM energy_boy ORDER BY tuoi DESC", null);
    }
    public Cursor get_energy_girls() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM energy_girl ORDER BY tuoi DESC", null);
    }
    public Cursor get_energy_recommend_boys() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM energy_recommend_boys ORDER BY tuoi DESC", null);
    }
    public Cursor get_energy_recommend_girls() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM energy_recommend_girls ORDER BY tuoi DESC", null);
    }
    public Cursor get_growth_data_boy() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM growth_data_boy ORDER BY tuoi DESC", null);
    }
    public Cursor get_growth_data_girl() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM growth_data_girl ORDER BY tuoi DESC", null);
    }
    // DatabaseHelper.java

    public Cursor get_snack() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT rowid AS _id, * FROM snack", null);
    }

    public Cursor get_meal() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT rowid AS _id, * FROM meal", null);
    }
    }