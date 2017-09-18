package com.timetracker.db.entries

object ActionEntry {
    const val TABLE_NAME = "actions"
    const val COLUMN_NAME_DATE = "date"
    const val COLUMN_NAME_CATEGORY_ID = "categoryId"
    const val COLUMN_NAME_TYPE = "type"
}

object CategoryEntry {
    const val TABLE_NAME = "categories"
    const val COLUMN_NAME_NAME = "name"
}

object GoalEntry {
    const val TABLE_NAME = "goals"
    const val COLUMN_CATEGORY_ID = "categoryId"
    const val COLUMN_TYPE = "type"
    const val COLUMN_DURATION = "duration"
}