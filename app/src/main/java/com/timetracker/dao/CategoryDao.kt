package com.timetracker.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

import com.timetracker.db.CategoriesContract
import com.timetracker.entities.Category

import java.util.ArrayList

import com.timetracker.db.CategoriesContract.CategoryEntry.*

class CategoryDao(private val dbHelper: SQLiteOpenHelper) {

    fun list(): List<Category> {
        val result = ArrayList<Category>()
        listCursor().use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                        Category(
                                cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                                cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME))
                        )
                )
            }
        }
        return result
    }

    fun listCursor(): Cursor {
        return dbHelper
                .readableDatabase
                .rawQuery("select * from " + TABLE_NAME, null)
    }

    fun save(createCategory: Category.CreateCategory) {
        dbHelper.writableDatabase.use { db ->
            val contentValues = ContentValues()
            contentValues.put(CategoriesContract.CategoryEntry.COLUMN_NAME_NAME, createCategory.name)
            db.insert(CategoriesContract.CategoryEntry.TABLE_NAME, null, contentValues)
        }
    }

    fun findByIdCursor(categoryId: Int?): Cursor {
        return dbHelper.readableDatabase
                .rawQuery(String.format("select * from %s where %s=%s",
                        TABLE_NAME, BaseColumns._ID, categoryId), null)
    }

    fun findById(categoryId: Int?): Category? {
        val cursor = findByIdCursor(categoryId)
        if (cursor.moveToNext())
            return currentCursorStateToCategory(cursor)
        else
            return null
    }

    private fun currentCursorStateToCategory(cursor: Cursor): Category {
        return Category(
                cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME))
        )
    }

}
