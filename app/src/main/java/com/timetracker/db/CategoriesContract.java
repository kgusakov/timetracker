package com.timetracker.db;

import android.provider.BaseColumns;

public class CategoriesContract {
    private CategoriesContract() {}

    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME_NAME = "name";
    }
}
