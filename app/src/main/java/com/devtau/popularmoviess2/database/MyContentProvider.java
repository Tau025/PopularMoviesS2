package com.devtau.popularmoviess2.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.devtau.popularmoviess2.utility.Logger;

public class MyContentProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String LOG_TAG = MyContentProvider.class.getSimpleName();
    private MySQLHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_BY_ID = 101;

    //задача QUERY_BUILDER в том чтобы сцепить все связанные таблицы воедино,
    //с тем чтобы дальше выполнять запросы к этой большой таблице
    private static final SQLiteQueryBuilder QUERY_BUILDER;
    static{
        QUERY_BUILDER = new SQLiteQueryBuilder();
        QUERY_BUILDER.setTables(MoviesTable.TABLE_NAME);
    }


    @Override
    public boolean onCreate() {
        Logger.d(LOG_TAG, "onCreate()");
        mOpenHelper = MySQLHelper.getInstance(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MySQLHelper.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MoviesTable.TABLE_NAME, MOVIE);
        matcher.addURI(authority, MoviesTable.TABLE_NAME + "/*", MOVIE_BY_ID);
        return matcher;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return MoviesTable.CONTENT_TYPE;
            case MOVIE_BY_ID:
                return MoviesTable.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Logger.d(LOG_TAG, "uri: " + String.valueOf(uri));

        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch(match) {
            case MOVIE: {
//                normalizeDate(values);
                long _id = db.insert(MoviesTable.TABLE_NAME, null, values);
                if(_id > 0) {
                    returnUri = MoviesTable.buildMovieUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Logger.v(LOG_TAG, "query(). uri: " + String.valueOf(uri));
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            // "movie"
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "movie/*"
            case MOVIE_BY_ID: {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE: {
//                normalizeDate(values);
                rowsUpdated = db.update(MoviesTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE: {
                rowsDeleted = db.delete(MoviesTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value: values) {
//                        normalizeDate(value);
                        long _id = db.insert(MoviesTable.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        String orderIdString = MoviesTable.getIdFromUri(uri);

        return QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                projection,
                MoviesTable.TABLE_NAME + "." + BaseColumns._ID + " = ? ",
                new String[]{orderIdString},
                null,
                null,
                sortOrder
        );
    }
}
