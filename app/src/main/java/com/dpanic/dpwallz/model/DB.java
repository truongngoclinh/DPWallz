package com.dpanic.dpwallz.model;

import android.database.Cursor;

/**
 * Created by dpanic on 9/29/2016.
 * Project: DPWallz
 */

final class DB {
    @SuppressWarnings("unused")
    public static final int BOOLEAN_FALSE = 0;
    static final int BOOLEAN_TRUE = 1;

    public static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    static boolean getBoolean(Cursor cursor, String columnName) {
        return getInt(cursor, columnName) == BOOLEAN_TRUE;
    }

    static long getLong(Cursor cursor, String columnName) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
    }

    private static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
    }

    private DB() {
        throw new AssertionError("No instances.");
    }
}
