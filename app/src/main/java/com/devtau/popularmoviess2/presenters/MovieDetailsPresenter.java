package com.devtau.popularmoviess2.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.model.Trailer;
import com.devtau.popularmoviess2.services.TrailersDownloaderService;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.view.MovieDetailsViewInterface;
import java.util.ArrayList;
/**
 * Презентер подробностей по выбранному фильму
 * Presenter of movie details
 */
public class MovieDetailsPresenter implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieDetailsPresenter.class.getSimpleName();
    private static final int LOADER_RESULTS = 556847;
    private MovieDetailsViewInterface view;
    private Movie movie;
    private long movieId;
    private BroadcastReceiver myBroadCastReceiver;

    public MovieDetailsPresenter(final MovieDetailsViewInterface view, long movieId) {
        this.view = view;
        this.movieId = movieId;
        myBroadCastReceiver = initBroadCastReceiver();
    }

    private BroadcastReceiver initBroadCastReceiver() {
        return new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                //На выполнение всего метода у нас есть 10 секунд, иначе появится ANR
                //To finish all operations in this method we have 10 seconds before ANR
                if(intent.getAction() == null && Constants.MY_BROADCAST_ACTION.equals(intent.getAction())
                        && intent.hasExtra(Intent.EXTRA_TEXT)) return;
                ArrayList<Trailer> trailersList = intent.getParcelableArrayListExtra(Constants.TRAILERS_LIST_EXTRA);
                view.populateTrailersList(trailersList);
            }
        };
    }

    //Отправим запрос контент-ресолверу за нужным курсором
    //Request a content resolver for a needed cursor
    public void restartLoader() {
        ((AppCompatActivity) view.getContext()).getSupportLoaderManager()
                .restartLoader(LOADER_RESULTS, null, this);
    }

    public void downloadTrailers() {
        TrailersDownloaderService.downloadTrailers(view.getContext(), movieId);
        registerBroadCastReceiver();
    }

    public void registerBroadCastReceiver() {
        //возобновим регистрацию широковещательного приёмника всякий раз при разворачивании приложения
        view.getContext().registerReceiver(myBroadCastReceiver, new IntentFilter(Constants.MY_BROADCAST_ACTION));
    }

    public void unregisterBroadCastReceiver() {
        //отменим регистрацию широковещательного приёмника всякий раз при сворачивании приложения
        view.getContext().unregisterReceiver(myBroadCastReceiver);
    }

    public void onFavoriteClick() {
        if(movie == null) {
            Logger.e(LOG_TAG, "onFavoriteClick(). movie is null");
            return;
        }

        movie.setIsFavorite(!movie.isFavorite());
        view.getContext().getContentResolver().update(MoviesTable.CONTENT_URI,
                MoviesTable.getContentValues(movie),
                BaseColumns._ID + "=?", new String[]{String.valueOf(movieId)});
    }


    //LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_RESULTS:
                if(movieId != 0) {
                    return new CursorLoader(view.getContext(), MoviesTable.buildMovieUri(movieId),
                            null, null, null, null);
                }
        }
        return null;
    }

    //onLoadFinished вызывается не только после завершения загрузки, вызванной методом restartLoader(),
    //но и каждый раз при обновлении базы
    //onLoadFinished is being called not only after restartLoader() from onCreate() is finished
    //but every time the db is being updated
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Logger.v(LOG_TAG, "onLoadFinished()");
        switch (loader.getId()) {
            case LOADER_RESULTS:
                //Попробуем сформировать заказ из курсора
                //Try to create a Movie object from received cursor
//                DatabaseUtils.dumpCursor(data);
                if(cursor != null) {
                    cursor.moveToFirst();
                    movie = new Movie(cursor);
                } else {
                    Logger.e(LOG_TAG, "onLoadFinished() received a null cursor");
                }

                //Обрабатывать вью-элементы связанной ативности нужно только если получен валидный фильм
                //Processing views of corresponding activity is necessary only if we've got a valid movie
                if (movie != null) {
                    view.populateUI(movie);
                } else {
                    Logger.e(LOG_TAG, "onLoadFinished() failed to create a valid movie from cursor");
                    view.showMessage(view.getContext().getString(R.string.movie_load_failed_msg));
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Logger.d(LOG_TAG, "onLoaderReset()");
    }
}
