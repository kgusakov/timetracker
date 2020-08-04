package com.timetracker.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

import com.timetracker.db.entries.*

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) = tableCreators.forEach { db.execSQL(it) }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion == 2) {
            db.execSQL("""CREATE TABLE ${GoalEntry.TABLE_NAME} (
                        ${BaseColumns._ID} INTEGER PRIMARY KEY,
                        ${GoalEntry.COLUMN_CATEGORY_ID} INTEGER NOT NULL,
                        ${GoalEntry.COLUMN_TYPE} TEXT NOT NULL,
                        ${GoalEntry.COLUMN_DURATION} INTEGER NOT NULL)""")
        }
    }

    companion object {

        val DATABASE_VERSION = 2
        val DATABASE_NAME = "Records.db"

        private val tableCreators = listOf(
                """CREATE TABLE ${ActionEntry.TABLE_NAME} (
                        ${BaseColumns._ID} INTEGER PRIMARY KEY,
                        ${ActionEntry.COLUMN_NAME_DATE} INTEGER,
                        ${ActionEntry.COLUMN_NAME_TYPE} TEXT,
                        ${ActionEntry.COLUMN_NAME_CATEGORY_ID} INTEGER)""",

                """CREATE TABLE ${CategoryEntry.TABLE_NAME} (
                        ${BaseColumns._ID} INTEGER PRIMARY KEY,
                        ${CategoryEntry.COLUMN_NAME_NAME} TEXT)""",

                """CREATE TABLE ${GoalEntry.TABLE_NAME} (
                        ${BaseColumns._ID} INTEGER PRIMARY KEY,
                        ${GoalEntry.COLUMN_CATEGORY_ID} INTEGER NOT NULL,
                        ${GoalEntry.COLUMN_TYPE} TEXT NOT NULL,
                        ${GoalEntry.COLUMN_DURATION} INTEGER NOT NULL)""")
    }

}
