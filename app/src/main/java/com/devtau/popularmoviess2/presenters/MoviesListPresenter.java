package com.devtau.popularmoviess2.presenters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.activities.MovieDetailsActivity;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.fragments.MovieDetailsFragment;
import com.devtau.popularmoviess2.model.SortBy;
import com.devtau.popularmoviess2.sync.SyncAdapter;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.view.MoviesListViewInterface;
/**
 * Презентер, показывающий список фильмов
 */
public class MoviesListPresenter implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SyncAdapter.SyncAdapterListener {
    private static final String LOG_TAG = MoviesListPresenter.class.getSimpleName();
    private static final int LOADER_RESULTS = 115297;
    private MoviesListViewInterface view;
    //счетчик попыток переподключения для метода retryConnection()
    private int counter;
    private SortBy sortBy = Constants.DEFAULT_SORT_BY;


    public MoviesListPresenter(MoviesListViewInterface view) {
        this.view = view;
    }

    public void sendRequestToServer() {
        if (checkIsOnline()){
            SyncAdapter.initializeSyncAdapter(view.getContext(), this);
        } else {
            view.showNoInternet();
        }
    }

    public boolean checkIsOnline() {
        ConnectivityManager cm = (ConnectivityManager) view.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    //NoInternetDFListener
    public void retryConnection(){
        //делаем несколько попыток повторного подключения с некоторым интервалом (см. Constants)
        view.showProgressBar();
        counter = 0;
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (checkIsOnline()) {
                    Logger.v(LOG_TAG, "Online!");
                    view.dismissProgressBar();
                    sendRequestToServer();
                } else if (counter < Constants.RETRY_COUNT) {
                    Logger.v(LOG_TAG, "Retrying connection. Counter: " + String.valueOf(counter));
                    counter++;
                    handler.postDelayed(this, Constants.RETRY_LAG);
                } else if (view.dismissProgressBar()){
                    //если все попытки не увенчались успехом, показываем диалог еще раз
                    view.showNoInternet();
                }
            }
        });
    }

    public void restartLoader() {
        //отправим запрос контент-ресолверу за нужным курсором
        ((AppCompatActivity) view.getContext()).getSupportLoaderManager()
                .restartLoader(LOADER_RESULTS, null, this);
    }

    public void onListItemClick(Cursor cursor, boolean twoPane) {
        //getItemAtPosition вернет полный курсор всех строк списка, но выставленный в нужную позицию
        //таким образом, вызывать cursor.moveToFirst не требуется
        //и можно сразу работать с активной строкой курсора
//        DatabaseUtils.dumpCursor(cursor);
        if(cursor != null) {
            long movieId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            Logger.v(LOG_TAG, "twoPane: " + String.valueOf(twoPane));
            if (twoPane) {
                Bundle arguments = new Bundle();
                arguments.putLong(MovieDetailsFragment.MOVIE_ID_EXTRA, movieId);
                MovieDetailsFragment fragment = new MovieDetailsFragment();
                fragment.setArguments(arguments);
                ((AppCompatActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_details_container, fragment)
                        .commit();
            } else {
                Intent intent = new Intent(view.getContext(), MovieDetailsActivity.class);
                intent.putExtra(MovieDetailsFragment.MOVIE_ID_EXTRA, movieId);
                view.getContext().startActivity(intent);
            }
        }
    }

    public String toggleSortOrder() {
        sortBy = SortBy.toggle(sortBy);
        SyncAdapter.syncImmediately(view.getContext());
        restartLoader();
        return sortBy.getDescription(view.getContext());
    }

    //SyncAdapterListener
    @Override
    public SortBy getSortBy() {
        return sortBy;
    }

    //LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_RESULTS:
                String sortOrder = sortBy.getDatabaseID() + " DESC";
                return new CursorLoader(view.getContext(), MoviesTable.CONTENT_URI,
                        null, null, null, sortOrder);
        }
        return null;
    }

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
