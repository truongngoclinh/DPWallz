package com.dpanic.dpwallz.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import rx.functions.Func1;

/**
 * Created by dpanic on 9/29/2016.
 * Project: DPWallz
 */

public class Category extends Entity implements Parcelable {

    static final String TABLE_NAME = "category";

    private static final String FIELD_PKEY = "_id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_LINK = "link";
    private static final String FIELD_THUMBLINK = "thumbLink";

    static final String CREATION_COMMAND =
            "CREATE TABLE " + TABLE_NAME + " (" + FIELD_PKEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FIELD_NAME + " TEXT NOT NULL, " +
                    FIELD_LINK + " TEXT NOT NULL, " +
                    FIELD_THUMBLINK + " TEXT NOT NULL )";
    static final Func1<Cursor, Category> MAPPER = new Func1<Cursor, Category>() {
        @Override
        public Category call(Cursor cursor) {
            return parseCursor(cursor);
        }
    };

    private String name;
    private String link;
    private String thumbLink;

    public Category(String name, String link, String thumbLink) {
        setName(name);
        setLink(link);
        setThumbLink(thumbLink);
    }

    public Category(long id, String name, String link, String thumbLink) {
        super(id);
        setName(name);
        setLink(link);
        setThumbLink(thumbLink);
    }

    protected Category(Parcel in) {
        super(in.readLong());
        setName(in.readString());
        setLink(in.readString());
        setThumbLink(in.readString());
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    private void setLink(String link) {
        this.link = link;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(getId());
        parcel.writeString(getName());
        parcel.writeString(getLink());
        parcel.writeString(getThumbLink());
    }

    private static Category parseCursor(Cursor cursor) {
        long id = DB.getLong(cursor, FIELD_PKEY);
        String name = DB.getString(cursor, FIELD_NAME);
        String link = DB.getString(cursor, FIELD_LINK);
        String thumbLink = DB.getString(cursor, FIELD_THUMBLINK);
        return new Category(id, name, link, thumbLink);
    }

    static ContentValues toContentValues(Category category) {
        ContentValues values = new ContentValues();
        values.put(FIELD_NAME, category.getName());
        values.put(FIELD_LINK, category.getLink());
        values.put(FIELD_THUMBLINK, category.getThumbLink());
        return values;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Category) {
            Category compareObj = (Category) obj;
            return compareObj.getName().equals(this.name);
        } else {
            return super.equals(obj);
        }
    }

    static void onUpgrade(SQLiteDatabase database, int updateVersion) {
        if (database == null) {
            return;
        }

        // Comment for later use
        Log.e("thanh.dao", "onUpgrade Category: version " + updateVersion);
        switch (updateVersion) {
        case 2:
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            database.execSQL(CREATION_COMMAND);
            break;
        default:
            break;
        }
    }

    public String getThumbLink() {
        return thumbLink;
    }

    private void setThumbLink(String thumbLink) {
        this.thumbLink = thumbLink;
    }
}
