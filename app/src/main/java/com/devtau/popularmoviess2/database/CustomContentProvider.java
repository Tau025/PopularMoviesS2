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
import android.util.Log;
import static com.devtau.popularmoviess2.database.CustomUriMatcher.*;

public class CustomContentProvider extends ContentProvider {
    
    //задача queryBuilder в том чтобы сцепить все связанные таблицы воедино,
    //с тем чтобы дальше выполнять запросы к этой большой таблице
    //queryBuilder job is to join all corresponding tables together
    //so that we could query the big resulting table
    private SQLiteQueryBuilder queryBuilder;
    private UriMatcher uriMatcher;
    private static final String LOG_TAG = "CustomContentProvider";
    private MySQLHelper dbHelper;


    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        queryBuilder = initQueryBuilder();
        uriMatcher = new CustomUriMatcher();
        dbHelper = MySQLHelper.getInstance(getContext());
        return true;
    }
    
    private SQLiteQueryBuilder initQueryBuilder() {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MoviesTable.TABLE_NAME);
        Log.v(LOG_TAG, "queryBuilder: " + String.valueOf(queryBuilder.getTables()));
        return queryBuilder;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
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
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "In insert(). uri is: " + String.valueOf(uri));

        final int match = uriMatcher.match(uri);
        Uri returnUri;

        switch(match) {
            case MOVIE: {
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
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "query(). uri: " + String.valueOf(uri));
        Cursor cursor;
        switch (uriMatcher.match(uri)) {

            // "movie"
            case MOVIE: {
                cursor = dbHelper.getReadableDatabase().query(
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
                cursor = getMovieById(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE: {
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
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;

        //Строка ниже позволит удалить все записи из этой таблицы
        //This makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE: {
                rowsDeleted = db.delete(MoviesTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case MOVIE: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
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
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        String movieIdString = MoviesTable.getIdFromUri(uri);
        return queryBuilder.query(dbHelper.getReadableDatabase(),
                projection,
                MoviesTable.TABLE_NAME + "." + BaseColumns._ID + " = ? ",
                new String[]{movieIdString},
                null,
                null,
                sortOrder
        );
    }
}
