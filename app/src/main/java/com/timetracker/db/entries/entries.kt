package com.timetracker.db.entries

import android.provider.BaseColumns

object ActionEntry : BaseColumns {
    const val TABLE_NAME = "actions"
    const val COLUMN_NAME_DATE = "date"
    const val COLUMN_NAME_CATEGORY_ID = "categoryId"
    const val COLUMN_NAME_TYPE = "type"
}

object CategoryEntry : BaseColumns {
    const val TABLE_NAME = "categories"
    const val COLUMN_NAME_NAME = "name"
}

object GoalEntry {
    const val TABLE_NAME = "actions"
    const val COLUMN_NAME_DATE = "date"
    const val COLUMN_NAME_CATEGORY_ID = "categoryId"
    const val COLUMN_NAME_TYPE = "type"
}