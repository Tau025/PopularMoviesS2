package com.devtau.popularmoviess2.presenters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.activities.MovieDetailsActivity;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.fragments.MovieDetailsFragment;
import com.devtau.popularmoviess2.fragments.NoInternetDF;
import com.devtau.popularmoviess2.model.SortBy;
import com.devtau.popularmoviess2.sync.SyncAdapter;
import com.devtau.popularmoviess2.util.Constants;
import com.devtau.popularmoviess2.util.Logger;
import com.devtau.popularmoviess2.view.MoviesListViewInterface;
/**
 * Презентер, показывающий список фильмов
 * Presenter for the movies list
 */
public class MoviesListPresenter implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NoInternetDF.NoInternetDFListener {
    private static final String LOG_TAG = MoviesListPresenter.class.getSimpleName();
    private static final int LOADER_RESULTS = 115297;
    private MoviesListViewInterface view;
    private int retryConnectionsCounter;
    private SortBy sortBy = Constants.DEFAULT_SORT_BY;


    public MoviesListPresenter(MoviesListViewInterface view) {
        this.view = view;
        sortBy = getSortBy();
    }

    public void sendRequestToServer() {
        if (checkIsOnline()){
            SyncAdapter.initializeSyncAdapter(view.getContext());
        } else {
            view.showNoInternetDF();
        }
    }

    public boolean checkIsOnline() {
        ConnectivityManager cm = (ConnectivityManager) view.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @Override
    public void retryConnection(){
        //Делаем несколько попыток повторного подключения с некоторым интервалом (см. Constants)
        //Make several attempts to find the net with some lag in between
        view.showProgressBarDF();
        retryConnectionsCounter = 0;
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (checkIsOnline()) {
                    Logger.v(LOG_TAG, "Online!");
                    view.dismissProgressBarDF();
                    view.showMessage(view.getContext().getString(R.string.online_msg));
                    sendRequestToServer();
                } else if (retryConnectionsCounter < Constants.RETRY_COUNT) {
                    Logger.v(LOG_TAG, "Retrying connection. Counter: " + String.valueOf(retryConnectionsCounter));
                    retryConnectionsCounter++;
                    handler.postDelayed(this, Constants.RETRY_LAG);
                } else if (view.dismissProgressBarDF()){
                    //Если все попытки не увенчались успехом, показываем диалог еще раз
                    //If all attempts had failed we show a dialog to user once more
                    view.showNoInternetDF();
                }
            }
        });
    }

    public void restartLoader() {
        //Отправим запрос контент-ресолверу за нужным курсором
        //Request a content resolver for a needed cursor
        ((AppCompatActivity) view.getContext()).getSupportLoaderManager()
                .restartLoader(LOADER_RESULTS, null, this);
    }

    public void onListItemClick(Cursor cursor, boolean twoPane) {
        //recyclerView.getChildLayoutPosition() вернет полный курсор всех строк списка,
        //но выставленный в нужную позицию. Таким образом, вызывать cursor.moveToFirst не требуется
        //и можно сразу работать с активной строкой курсора
        //recyclerView.getChildLayoutPosition() returns full cursor of listed items but it will be
        //moved to position of selected list item. Hence client don't need to use cursor.moveToFirst
        //and is able to work with active cursor row right off
//        DatabaseUtils.dumpCursor(cursor);
        if(cursor != null) {
            long movieId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            Logger.v(LOG_TAG, "twoPane: " + String.valueOf(twoPane));
            if (twoPane) {
                Bundle arguments = new Bundle();
                arguments.putLong(Constants.MOVIE_ID_EXTRA, movieId);
                MovieDetailsFragment fragment = new MovieDetailsFragment();
                fragment.setArguments(arguments);
                ((AppCompatActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_details_container, fragment)
                        .commit();
            } else {
                Intent intent = new Intent(view.getContext(), MovieDetailsActivity.class);
                intent.putExtra(Constants.MOVIE_ID_EXTRA, movieId);
                view.getContext().startActivity(intent);
            }
        }
    }

    public void switchSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        prefs.edit().putLong(SortBy.SORT_BY_EXTRA, sortBy.getId()).apply();
        restartLoader();
    }

    public SortBy getSortBy() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        long sortById = prefs.getLong(SortBy.SORT_BY_EXTRA, Constants.DEFAULT_SORT_BY.getId());
        return SortBy.getById(sortById);
    }


    //LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_RESULTS:
                if(sortBy == SortBy.MOST_POPULAR || sortBy == SortBy.TOP_RATED) {
                    String sortOrder = sortBy.getDatabaseID() + " DESC LIMIT 20";
                    return new CursorLoader(view.getContext(), MoviesTable.CONTENT_URI, null, null, null, sortOrder);
                } else if(sortBy == SortBy.IS_FAVORITE) {
                    return new CursorLoader(view.getContext(), MoviesTable.CONTENT_URI, null, MoviesTable.IS_FAVORITE + "=?", new String[]{"1"}, null);
                }
        }
        return null;
    }

    //onLoadFinished вызывается не только после завершения загрузки, вызванной методом restartLoader(),
    //но и каждый раз при обновлении базы
    //onLoadFinished is being called not only after restartLoader() from onCreate() is finished
    //but every time the db is being updated
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        Logger.v(LOG_TAG, "onLoadFinished()");
        switch (loader.getId()) {
            case LOADER_RESULTS:
                view.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_RESULTS:
                view.swapCursor(null);
                break;
        }
    }
}
