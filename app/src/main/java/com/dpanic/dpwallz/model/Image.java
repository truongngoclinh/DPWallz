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

public class Image extends Entity implements Parcelable {
    static final String TABLE_NAME = "image";

    static final String FIELD_PKEY = "_id";
    static final String FIELD_PEXELS_ID = "pexels_id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ORIGINAL_LINK = "original_link";
    private static final String FIELD_LARGE_LINK = "large_link";
    private static final String FIELD_DETAIL_LINK = "detail_link";
    static final String FIELD_LOCAL_LINK = "local_link";
    static final String FIELD_IS_FAVORITE = "is_favorite";

    static final String CREATION_COMMAND =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + FIELD_PKEY + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + FIELD_PEXELS_ID + " TEXT NOT NULL, "
                    + FIELD_NAME + " TEXT NOT NULL, "
                    + FIELD_ORIGINAL_LINK + " TEXT NOT NULL, "
                    + FIELD_LARGE_LINK + " TEXT NOT NULL, "
                    + FIELD_DETAIL_LINK + " TEXT NOT NULL, "
                    + FIELD_LOCAL_LINK + " TEXT, "
                    + FIELD_IS_FAVORITE + " INTEGER NOT NULL"
                    + " )";

    static final Func1<Cursor, Image> MAPPER = new Func1<Cursor, Image>() {
        @Override
        public Image call(Cursor cursor) {
            return parseCursor(cursor);
        }
    };

    private String pexelId;
    private String name;
    private String originalLink;
    private String largeLink;
    private String detailLink;
    private String localLink;
    private boolean isFavorite;

    public Image() {
    }

    public Image(String pexelId, String name, String originalLink, String largeLink, String detailLink, String
            localLink, boolean
                         isFavorite) {
        setPexelId(pexelId);
        setName(name);
        setOriginalLink(originalLink);
        setLargeLink(largeLink);
        setDetailLink(detailLink);
        setLocalLink(localLink);
        setFavorite(isFavorite);
    }

    public Image(long id, String pexelId, String name, String originalLink, String largeLink, String detailLink,
                 String localLink,
                 boolean isFavorite) {
        super(id);
        setPexelId(pexelId);
        setName(name);
        setOriginalLink(originalLink);
        setLargeLink(largeLink);
        setDetailLink(detailLink);
        setLocalLink(localLink);
        setFavorite(isFavorite);
    }

    private Image(Parcel in) {
        super(in.readLong());
        setPexelId(in.readString());
        setName(in.readString());
        setOriginalLink(in.readString());
        setLargeLink(in.readString());
        setDetailLink(in.readString());
        setLocalLink(in.readString());
        setFavorite(in.readInt() == 1);
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    String getPexelId() {
        return pexelId;
    }

    private void setPexelId(String pexelId) {
        this.pexelId = pexelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    private void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(getId());
        parcel.writeString(getPexelId());
        parcel.writeString(getName());
        parcel.writeString(getOriginalLink());
        parcel.writeString(getLargeLink());
        parcel.writeString(getDetailLink());
        parcel.writeString(getLocalLink());
        parcel.writeInt(isFavorite() ? 1 : 0);
    }

    static Image parseCursor(Cursor cursor) {
        long id = DB.getLong(cursor, FIELD_PKEY);
        String pexelId = DB.getString(cursor, FIELD_PEXELS_ID);
        String name = DB.getString(cursor, FIELD_NAME);
        String originalLink = DB.getString(cursor, FIELD_ORIGINAL_LINK);
        String largeLink = DB.getString(cursor, FIELD_LARGE_LINK);
        String detailLink = DB.getString(cursor, FIELD_DETAIL_LINK);
        String localLink = DB.getString(cursor, FIELD_LOCAL_LINK);
        boolean isFavorite = DB.getBoolean(cursor, FIELD_IS_FAVORITE);

        return new Image(id, pexelId, name, originalLink, largeLink, detailLink, localLink, isFavorite);
    }

    static ContentValues toContentValues(Image image) {
        ContentValues values = new ContentValues();

        values.put(FIELD_PEXELS_ID, image.getPexelId());
        values.put(FIELD_NAME, image.getName());
        values.put(FIELD_ORIGINAL_LINK, image.getOriginalLink());
        values.put(FIELD_LARGE_LINK, image.getLargeLink());
        values.put(FIELD_DETAIL_LINK, image.getDetailLink());
        values.put(FIELD_LOCAL_LINK, image.getLocalLink());
        values.put(FIELD_IS_FAVORITE, image.isFavorite() ? 1 : 0);

        return values;
    }

    public String getLargeLink() {
        return largeLink;
    }

    private void setLargeLink(String largeLink) {
        this.largeLink = largeLink;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getDetailLink() {
        return detailLink;
    }

    private void setDetailLink(String detailLink) {
        this.detailLink = detailLink;
    }

    public String getLocalLink() {
        return localLink;
    }

    public void setLocalLink(String localLink) {
        this.localLink = localLink;
    }

    static void onUpgrade(SQLiteDatabase database, int updateVersion) {
        if (database == null) {
            return;
        }

        Log.e("thanh.dao", "onUpgrade Image: version " + updateVersion);
        // Comment for later use
        switch (updateVersion) {
        default:
            break;
        }
    }
}
