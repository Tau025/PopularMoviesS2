package com.devtau.popularmoviess2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import com.devtau.popularmoviess2.util.Logger;
/**
 * Helper is one no matter how much tables there are
 */
public class MySQLHelper extends SQLiteOpenHelper {
    
    public static final String DB_NAME = "PopularMoviesS2";
    private static final int DB_VERSION = 1;
    private static final String LOG_TAG = "MySQLHelper";
    private static MySQLHelper instance;
    public static final String CREATE_TABLE = "CREATE TABLE %s (%s);";
    public static final String PRIMARY_KEY = BaseColumns._ID + " integer primary key autoincrement, ";

//    see also strings.content_authority
    public static final String CONTENT_AUTHORITY = "com.devtau.popularmoviess2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    //singleton protects db from multi thread concurrent access
    private MySQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static MySQLHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MySQLHelper(context);
        }
        return instance;
    }

    //note, that onCreate() is being called only when you put something to db
    //so calling the constructor does not construct nor tables nor db itself if there is nothing to store
    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
        Logger.d(LOG_TAG, "onCreate()");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d(LOG_TAG, "Found new DB version. About to update to: " + String.valueOf(DB_VERSION));
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            if (oldVersion < 1) {
                //здесь перечисляем создаваемые таблицы
                db.execSQL(getCreateSql(MoviesTable.TABLE_NAME, MoviesTable.FIELDS));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private String getCreateSql(String tableName, String fields) {
        return String.format(CREATE_TABLE, tableName, fields);
    }
}
