package com.devtau.popularmoviess2.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.database.MySQLHelper;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.model.SortBy;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.utility.NetworkHelper;
import java.net.URL;
import java.util.List;
/**
 * Адаптер, позволяющий синхронизироваться с сервером в фоне по заданному расписанию
 * Adapter that provides the ability to sync with server asynchronously at given intervals
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public final static String LOG_TAG = SyncAdapter.class.getSimpleName();
    private static final int MOVIE_UPDATED = 1;
    private static final int MOVIE_CREATED = 2;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Logger.d(LOG_TAG, "Starting sync");

        //Сначала запросим и обработаем список самых популярных фильмов
        //First we query and process most popular movies list
        URL moviesListEndpointPopular = NetworkHelper.getMoviesListUrl(getContext(), SortBy.MOST_POPULAR);
        Logger.d(LOG_TAG, "moviesListEndpointPopular: " + String.valueOf(moviesListEndpointPopular));
        if(moviesListEndpointPopular == null) {
            Logger.e(LOG_TAG, "Can't start download with null URL");
            return;
        }

        String jsonStringPopular = NetworkHelper.requestJSONStringFromServer(moviesListEndpointPopular);
        Logger.v(LOG_TAG, "jsonString: " + String.valueOf(jsonStringPopular));
        if (TextUtils.isEmpty(jsonStringPopular) || "".equals(jsonStringPopular)) {
            Logger.e(LOG_TAG, "Can't parse not valid jsonString");
            return;
        }
        List<Movie> parsedMoviesListPopular = NetworkHelper.parseMoviesListJSON(jsonStringPopular);
        if(parsedMoviesListPopular == null || parsedMoviesListPopular.size() == 0) {
            Logger.e(LOG_TAG, "parsedMoviesListPopular is null or empty. Terminating service");
            return;
        }
        updateOrCreateMoviesList(parsedMoviesListPopular);


        //Потом запросим и обработаем список фильмов с самым высоким рейтингом
        //Then we query and process top rated movies list
        URL moviesListEndpointTopRated = NetworkHelper.getMoviesListUrl(getContext(), SortBy.TOP_RATED);
        Logger.d(LOG_TAG, "moviesListEndpointTopRated: " + String.valueOf(moviesListEndpointTopRated));
        if(moviesListEndpointTopRated == null) {
            Logger.e(LOG_TAG, "Can't start download with null URL");
            return;
        }

        String jsonStringTopRated = NetworkHelper.requestJSONStringFromServer(moviesListEndpointTopRated);
        Logger.v(LOG_TAG, "jsonStringTopRated: " + String.valueOf(jsonStringTopRated));
        if (TextUtils.isEmpty(jsonStringTopRated) || "".equals(jsonStringTopRated)) {
            Logger.e(LOG_TAG, "Can't parse not valid jsonString");
            return;
        }

        List<Movie> parsedMoviesListTopRated = NetworkHelper.parseMoviesListJSON(jsonStringTopRated);
        if(parsedMoviesListTopRated == null || parsedMoviesListTopRated.size() == 0) {
            Logger.e(LOG_TAG, "parsedMoviesListTopRated is null or empty. Terminating service");
            return;
        }
        updateOrCreateMoviesList(parsedMoviesListTopRated);
    }


    private void updateOrCreateMoviesList(List<Movie> parsedMovies) {
        int moviesUpdated = 0;
        int moviesCreated = 0;
        for(Movie parsedMovie: parsedMovies) {
            int updateOrCreateMovieResponse = updateOrCreateMovie(parsedMovie);
            if(updateOrCreateMovieResponse == MOVIE_UPDATED) {
                moviesUpdated++;
            } else if(updateOrCreateMovieResponse == MOVIE_CREATED) {
                moviesCreated++;
            }
        }
        Logger.v(LOG_TAG, "updateOrCreateMoviesList() finished."
                + " moviesUpdated: " + String.valueOf(moviesUpdated)
                + ", moviesCreated: " + String.valueOf(moviesCreated));
    }

    //Метод обновит в базе приложения существующий или добавит новый фильм
    //Updates or creates new Movie in our app db
    private int updateOrCreateMovie(Movie parsedMovie) {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(MoviesTable.buildMovieUri(parsedMovie.getId()), null, null, null, null);
        DatabaseUtils.dumpCursor(cursor);

        if(cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            Movie oldMovie = new Movie(cursor);
            cursor.close();
            oldMovie.updateFields(parsedMovie);
            cr.update(MoviesTable.CONTENT_URI, MoviesTable.getContentValues(oldMovie),
                    BaseColumns._ID + "=?", new String[]{String.valueOf(parsedMovie.getId())});
            return MOVIE_UPDATED;
        } else {
            cr.insert(MoviesTable.CONTENT_URI, MoviesTable.getContentValues(parsedMovie));
            return MOVIE_CREATED;
        }
    }


    //Глобальная точка доступа к SyncAdapter
    //Global access point to SyncAdapter class
    public static void initializeSyncAdapter(Context context) {
        Logger.v(LOG_TAG, "In initializeSyncAdapter()");
        getSyncAccount(context);
    }

    //Метод возвратит учетную запись для настройки синхронизации или создаст новую, если ее еще нет
    //Provides the fake account to be used with SyncAdapter, or creates a new one if there is none
    @Nullable
    private static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if(null == accountManager.getPassword(newAccount)) {
            //Если нет пароля, то нет и учетной записи.
            //Создадим новый аккаунт с пустым пролем и без userdata
            //If there's no password, the account doesn't exist.
            //So we add the account and account type, no password or user data
            if(!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Logger.e(LOG_TAG, "Failed to create account");
                return null;
            }
            SyncAdapter.configurePeriodicSync(context, Constants.SYNC_INTERVAL, Constants.SYNC_FLEXTIME);
            ContentResolver.setSyncAutomatically(newAccount, MySQLHelper.CONTENT_AUTHORITY, true);
            syncImmediately(context);
        }
        return newAccount;
    }

    //Метод запланирует синхронизацию с заданным временным интервалом
    //Schedules the sync adapter periodic execution
    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        if (Build.VERSION.SDK_INT >= 19) {
            //Начиная с АПИ 19+ синхронизацию следует настравать с неточным таймером для экономии батареии
            //Since API 19+ we should enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, MySQLHelper.CONTENT_AUTHORITY).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, MySQLHelper.CONTENT_AUTHORITY, new Bundle(), syncInterval);
        }
    }

    private static void syncImmediately(Context context) {
        Logger.v(LOG_TAG, "In syncImmediately()");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), MySQLHelper.CONTENT_AUTHORITY, bundle);
    }
}