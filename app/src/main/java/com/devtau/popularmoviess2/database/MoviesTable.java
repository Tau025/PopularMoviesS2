package com.devtau.popularmoviess2.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.utility.Utility;

public abstract class MoviesTable {
    public static final String TABLE_NAME = "Movies";

    public static final String TITLE = "title";
    public static final String POSTER_PATH = "posterPath";
    public static final String PLOT_SYNOPSIS = "plotSynopsis";
    public static final String USER_RATING = "userRating";
    public static final String POPULARITY = "popularity";
    public static final String RELEASE_DATE = "releaseDate";
    public static final String IS_FAVORITE = "isFavorite";

    public static final String FIELDS = MySQLHelper.PRIMARY_KEY
            + TITLE + " TEXT NOT NULL, "
            + POSTER_PATH + " TEXT NOT NULL, "
            + PLOT_SYNOPSIS + " TEXT NOT NULL, "
            + USER_RATING + " REAL NOT NULL, "
            + POPULARITY + " REAL NOT NULL, "
            + RELEASE_DATE + " TEXT NOT NULL, "
            + IS_FAVORITE + " INTEGER NOT NULL";

    public static final Uri CONTENT_URI =
            MySQLHelper.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MySQLHelper.CONTENT_AUTHORITY + "/" + TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MySQLHelper.CONTENT_AUTHORITY + "/" + TABLE_NAME;
    private static final String LOG_TAG = MoviesTable.class.getSimpleName();

    public static Uri buildMovieUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static String getIdFromUri(Uri uri) {
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
        cv.put(POPULARITY, item.getPopularity());
        cv.put(RELEASE_DATE, Utility.dateFormat.format(item.getReleaseDate().getTime()));
        cv.put(IS_FAVORITE, item.isFavorite() ? 1 : 0);
        return cv;
    }
}