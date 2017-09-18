package com.timetracker.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteOpenHelper
import com.timetracker.db.entries.CategoryEntry
import com.timetracker.db.entries.GoalEntry
import com.timetracker.entities.CreateGoal

class GoalDao(private val dbHelper: SQLiteOpenHelper) {

    fun save(createGoal: CreateGoal) {
        dbHelper.writableDatabase.use { db ->
            val contentValues = ContentValues()
            contentValues.put(GoalEntry.COLUMN_CATEGORY_ID, createGoal.catergoryId)
            contentValues.put(GoalEntry.COLUMN_TYPE, createGoal.type)
            db.insert(CategoryEntry.TABLE_NAME, null, contentValues)
        }
    }
}