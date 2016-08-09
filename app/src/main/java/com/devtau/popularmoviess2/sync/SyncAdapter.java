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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.database.MySQLHelper;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.model.SortBy;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.utility.Utility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
/**
 * Адаптер, позволяющий синхронизироваться с сервером в фоне по заданному расписанию
 * Adapter that provides the ability to sync with server asynchronously at given intervals
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public final static String LOG_TAG = SyncAdapter.class.getSimpleName();
    private static final int MOVIE_UPDATED = 1;
    private static final int MOVIE_CREATED = 2;
    private static SyncAdapterListener listener;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Logger.d(LOG_TAG, "Starting sync");

        //urlConnection и reader должы объявляться вне try/catch блока,
        //чтобы их можно было закрыть в блоке finally
        //urlConnection & reader need to be declared outside the try/catch
        //so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        SortBy sortBy = Constants.DEFAULT_SORT_BY;
        if(listener != null) sortBy = listener.getSortBy();
        String sortByString = sortBy.getKeyID(getContext());

        try {
            //Подготовим URL для запроса к серверу themoviedb.org
            //Construct the URL for themoviedb.org query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(sortByString)
                    .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY_VALUE);
            URL url = new URL(builder.build().toString());
            Logger.d(LOG_TAG, "url: " + String.valueOf(url));

            //Создадим запрос к серверу themoviedb.org и откроем urlConnection
            //Create the request to themoviedb.org, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Создадим inputStream и сохраним его содержимое в строку используя буфер
            //Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                //Нет смысла парсить пустой JSON. Выходим из метода
                //Stream was empty. No point in parsing.
                return;
            }

            String jsonString = buffer.toString();
            List<Movie> parsedMovies = parseJson(jsonString);
            updateOrCreateMoviesList(parsedMovies);

        } catch (IOException e) {
            Logger.e(LOG_TAG, e);
        } catch (JSONException e) {
            Logger.e(LOG_TAG, e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Logger.e(LOG_TAG, "while closing stream", e);
                }
            }
        }
    }

    //Метод распарсит JSON строку и вернет List фильмов, перечисленных в предоставленной JSON строке
    //Parses JSON string to a List of Movies
    private List<Movie> parseJson(String JSONString) throws JSONException {
        JSONObject serverAnswer = new JSONObject(JSONString);
        JSONArray moviesJsonArray = serverAnswer.getJSONArray(Constants.JSON_RESULTS);

        List<Movie> moviesList = new ArrayList<>();
        for(int i = 0; i < moviesJsonArray.length(); i++) {
            JSONObject JSONMovie = moviesJsonArray.getJSONObject(i);
            long movieID = JSONMovie.getLong(Constants.JSON_ID);
            String movieTitle = JSONMovie.getString(Constants.JSON_TITLE);
            String moviePosterPath = JSONMovie.getString(Constants.JSON_POSTER_PATH);
            String moviePlotSynopsis = JSONMovie.getString(Constants.JSON_PLOT_SYNOPSIS);
            double movieUserRating = JSONMovie.getDouble(Constants.JSON_USER_RATING);
            double moviePopularity = JSONMovie.getDouble(Constants.JSON_POPULARITY);

            Calendar movieReleaseDate = new GregorianCalendar();
            try {
                movieReleaseDate = new GregorianCalendar(1970, 0, 1);
                String dateString = JSONMovie.getString(Constants.JSON_RELEASE_DATE);
                movieReleaseDate.setTime(Utility.theMovieDBDateFormat.parse(dateString));
            } catch (ParseException e) {
                Logger.e(LOG_TAG, "while parsing releaseDate from JSON", e);
            }

            //Соберем из всех подготовленных компонентов объект класса Movie
            //Create a Movie from all fields that we have now
            Movie parsedMovie = new Movie(movieID, movieTitle, moviePosterPath,
                    moviePlotSynopsis, movieUserRating, moviePopularity, movieReleaseDate);
            moviesList.add(parsedMovie);
        }
        return moviesList;
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
//        DatabaseUtils.dumpCursor(cursor);

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
    public static void initializeSyncAdapter(Context context, SyncAdapterListener listener) {
        Logger.v(LOG_TAG, "In initializeSyncAdapter()");
        try {
            SyncAdapter.listener = listener;
        } catch (Exception e) {
            Logger.e(LOG_TAG, "While initializing listener", e);
        }
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

    public static void syncImmediately(Context context) {
        Logger.v(LOG_TAG, "In syncImmediately()");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), MySQLHelper.CONTENT_AUTHORITY, bundle);
    }

    public interface SyncAdapterListener{
        SortBy getSortBy();
    }
}