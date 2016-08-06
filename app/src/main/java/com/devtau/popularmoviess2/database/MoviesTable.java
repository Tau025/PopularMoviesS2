package com.devtau.popularmoviess2.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.util.Logger;
import com.devtau.popularmoviess2.util.Util;

public abstract class MoviesTable {
    public static final String TABLE_NAME = "Movies";

    public static final String TITLE = "title";
    public static final String POSTER_PATH = "posterPath";
    public static final String PLOT_SYNOPSIS = "plotSynopsis";
    public static final String USER_RATING = "userRating";
    public static final String RELEASE_DATE = "releaseDate";

    public static final String FIELDS = MySQLHelper.PRIMARY_KEY
            + TITLE + " TEXT, "
            + POSTER_PATH + " TEXT, "
            + PLOT_SYNOPSIS + " TEXT, "
            + USER_RATING + " REAL, "
            + RELEASE_DATE + " TEXT";

    public static final Uri CONTENT_URI =
            MySQLHelper.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MySQLHelper.CONTENT_AUTHORITY + "/" + TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MySQLHelper.CONTENT_AUTHORITY + "/" + TABLE_NAME;
    private static final String LOG_TAG = MoviesTable.class.getSimpleName();

    public static Uri buildOrderUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static String getOrderIdFromUri(Uri uri) {
        Logger.v(LOG_TAG, "getOrderIdFromUri(). segment 1: " + String.valueOf(uri.getPathSegments().get(1)));
        return uri.getPathSegments().get(1);
    }

    public static ContentValues getContentValues(Movie item) {
        ContentValues cv = new ContentValues();
        if (item.getId() != -1) {
            cv.put(BaseColumns._ID, item.getId());
        }
        cv.put(TITLE, item.getTitle());
        cv.put(POSTER_PATH, item.getPosterPath());
        cv.put(PLOT_SYNOPSIS, item.getPlotSynopsis());
        cv.put(USER_RATING, item.getUserRating());
        cv.put(RELEASE_DATE, Util.dateFormat.format(item.getReleaseDate().getTime()));
        return cv;
    }
}