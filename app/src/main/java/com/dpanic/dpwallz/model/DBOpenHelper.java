package com.dpanic.dpwallz.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dpanic on 9/29/2016.
 * Project: DPWallz
 */

class DBOpenHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "dpwallz.db";
    private static DBOpenHelper dbHelper;

    public static DBOpenHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBOpenHelper(context.getApplicationContext());
        }

        return dbHelper;
    }

    private DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Image.CREATION_COMMAND);
        db.execSQL(Category.CREATION_COMMAND);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // db is out of date; update db according to corresponding version
        if (oldVersion < newVersion) {
            while (oldVersion < newVersion) {
                int updateVersion = ++oldVersion;
                Category.onUpgrade(db, updateVersion);
                Image.onUpgrade(db, updateVersion);
            }
        }
    }
}
