package com.timetracker.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.timetracker.db.CategoriesContract;
import com.timetracker.entities.Action;
import com.timetracker.entities.Category;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.timetracker.db.CategoriesContract.CategoryEntry.*;

public class CategoryDao {

    private final SQLiteOpenHelper dbHelper;

    public CategoryDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Stream<Category> list() {
        List<Category> result = new ArrayList<>();
        try(Cursor cursor = listCursor()) {
            while(cursor.moveToNext()) {
                result.add(
                        new Category(
                                cursor.getInt(cursor.getColumnIndex(_ID)),
                                cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME))
                        )
                );
            }
        }
        return result.stream();
    }

    public Cursor listCursor() {
        return dbHelper
            .getReadableDatabase()
            .rawQuery("select * from " + TABLE_NAME, null);
    }

    public void save(Category.CreateCategory createCategory) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CategoriesContract.CategoryEntry.COLUMN_NAME_NAME, createCategory.name);
            db.insert(CategoriesContract.CategoryEntry.TABLE_NAME, null, contentValues);
        }
    }

    public Cursor findByIdCursor(Integer categoryId) {
        return dbHelper.getReadableDatabase()
                .rawQuery(String.format("select * from %s where %s=%s",
                        TABLE_NAME, _ID, categoryId), null);
    }

    public Optional<Category> findById(Integer categoryId) {
        Cursor cursor = findByIdCursor(categoryId);
        if (cursor.moveToNext()) return Optional.of(currentCursorStateToCategory(cursor));
        else return Optional.empty();
    }

    private Category currentCursorStateToCategory(Cursor cursor) {
        return new Category(
                cursor.getInt(cursor.getColumnIndex(_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME))
        );
    }

}
