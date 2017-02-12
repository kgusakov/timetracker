package com.timetracker.db;

import android.provider.BaseColumns;

public final class ActionsContract {

    private ActionsContract() {}

    public static class ActionEntry implements BaseColumns {
        public static final String TABLE_NAME = "actions";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_CATEGORY_ID = "categoryId";
        public static final String COLUMN_NAME_TYPE = "type";
    }
}
