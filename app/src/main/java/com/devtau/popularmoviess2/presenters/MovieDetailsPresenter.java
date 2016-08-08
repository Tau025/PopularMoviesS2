package com.devtau.popularmoviess2.presenters;

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
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.view.MovieDetailsViewInterface;
/**
 * Презентер подробностей по выбранному фильму
 */
public class MovieDetailsPresenter implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieDetailsPresenter.class.getSimpleName();
    private static final int LOADER_RESULTS = 556847;
    private MovieDetailsViewInterface view;
    private Movie movie;
    private long movieId;

    public MovieDetailsPresenter(MovieDetailsViewInterface view, long movieId) {
        this.view = view;
        this.movieId = movieId;
    }

    //отправим запрос контент-ресолверу за нужным курсором
    public void restartLoader() {
        ((AppCompatActivity) view.getContext()).getSupportLoaderManager()
                .restartLoader(LOADER_RESULTS, null, this);
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

    //onLoadFinished is being called not only after restartLoader() from onCreate() is finished
    //but every time the db is being updated
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Logger.v(LOG_TAG, "onLoadFinished()");
        switch (loader.getId()) {
            case LOADER_RESULTS:
                //попробуем сформировать заказ из курсора
//                DatabaseUtils.dumpCursor(data);
                if(data != null) {
                    data.moveToFirst();
                    movie = new Movie(data);
                }

                //обрабатывать вью-элементы этой ативности нужно только если получен валидный заказ
                if (movie != null) {
                    view.populateUI(movie);
                } else {
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
