package com.timetracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.timetracker.db.ActionsContract.ActionEntry;

import java.util.Arrays;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Records.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ACTIONS);
        db.execSQL(SQL_CREATE_CATEGORIES);
        for (String category: CATEGORIES) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CategoriesContract.CategoryEntry.COLUMN_NAME_NAME, category);
            db.insert(CategoriesContract.CategoryEntry.TABLE_NAME, null, contentValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static final String SQL_CREATE_ACTIONS =
            "CREATE TABLE " + ActionEntry.TABLE_NAME + " (" +
                    ActionEntry._ID + " INTEGER PRIMARY KEY," +
                    ActionEntry.COLUMN_NAME_DATE + " INTEGER," +
                    ActionEntry.COLUMN_NAME_TYPE + " TEXT," +
                    ActionEntry.COLUMN_NAME_CATEGORY_ID + " INTEGER)";

    private static final String SQL_CREATE_CATEGORIES =
            "CREATE TABLE " + CategoriesContract.CategoryEntry.TABLE_NAME + " (" +
                    CategoriesContract.CategoryEntry._ID + " INTEGER PRIMARY KEY," +
                    CategoriesContract.CategoryEntry.COLUMN_NAME_NAME + " TEXT)";

    private static final List<String> CATEGORIES = Arrays.asList("technical reading", "ukulele");
}
