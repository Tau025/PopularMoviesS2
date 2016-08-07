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

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public final static String LOG_TAG = SyncAdapter.class.getSimpleName();
    // Interval at which to sync with server, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int NOTIFICATION_ID = 275236;
    private static SyncAdapterListener listener;

    private static final String[] NOTIFICATION_PROJECTION = new String[] {
            MoviesTable.TITLE,
            MoviesTable.POSTER_PATH,
            MoviesTable.PLOT_SYNOPSIS,
            MoviesTable.USER_RATING,
            MoviesTable.RELEASE_DATE
    };

    // these indices must match the projection
    private static final int INDEX_TITLE = 0;
    private static final int INDEX_POSTER_PATH = 1;
    private static final int INDEX_PLOT_SYNOPSIS = 2;
    private static final int INDEX_USER_RATING = 3;
    private static final int INDEX_RELEASE_DATE = 4;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Logger.d(LOG_TAG, "Starting sync");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        SortBy sortBy = Constants.DEFAULT_SORT_BY;
        if(listener != null) sortBy = listener.getSortBy();
        String sortByString = sortBy.getKeyID(getContext());

        try {
            // Construct the URL for themoviedb.org query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(sortByString)
                    .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY_VALUE);
            URL url = new URL(builder.build().toString());
            Logger.d(LOG_TAG, "url: " + String.valueOf(url));

            // Create the request to themoviedb.org, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }

            // Will contain the raw JSON response as a string.
            String jsonString = buffer.toString();
            parseJson(jsonString);

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

            //create a Movie from all fields that we have now
            Movie parsedMovie = new Movie(movieID, movieTitle, moviePosterPath,
                    moviePlotSynopsis, movieUserRating, moviePopularity, movieReleaseDate);
            parsedMovie = updateOrCreateMovie(parsedMovie);
            moviesList.add(parsedMovie);
        }
        return moviesList;
    }

    //updates or creates new Movie in our app db
    private Movie updateOrCreateMovie(Movie parsedMovie) {
        Logger.d(LOG_TAG, "parsedMovie.getPopularity(): " + String.valueOf(parsedMovie.getPopularity()));
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(MoviesTable.buildOrderUri(parsedMovie.getId()), null, null, null, null);
        DatabaseUtils.dumpCursor(cursor);

        if(cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            Movie oldMovie = new Movie(cursor);
            cursor.close();
            oldMovie.updateFields(parsedMovie);
            cr.update(MoviesTable.CONTENT_URI, MoviesTable.getContentValues(oldMovie),
                    BaseColumns._ID + "=?", new String[]{String.valueOf(parsedMovie.getId())});
            return oldMovie;
        } else {
            cr.insert(MoviesTable.CONTENT_URI, MoviesTable.getContentValues(parsedMovie));
            return parsedMovie;
        }
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Logger.v(LOG_TAG, "in configurePeriodicSync()");
        Account account = getSyncAccount(context);
        String authority = MySQLHelper.CONTENT_AUTHORITY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    @Nullable
    public static Account getSyncAccount(Context context) {
        Logger.v(LOG_TAG, "in getSyncAccount()");
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Logger.v(LOG_TAG, "in onAccountCreated()");
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        //Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, MySQLHelper.CONTENT_AUTHORITY, true);

        syncImmediately(context);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Logger.v(LOG_TAG, "in syncImmediately()");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), MySQLHelper.CONTENT_AUTHORITY, bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        Logger.v(LOG_TAG, "in initializeSyncAdapter()");
        try {
            listener = (SyncAdapterListener) context;
        } catch (Exception e) {
            Logger.e(LOG_TAG, "while initializing listener", e);
        }
        getSyncAccount(context);
    }

    public interface SyncAdapterListener{
        SortBy getSortBy();
    }
}