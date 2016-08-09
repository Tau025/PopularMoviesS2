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
 * Presenter of movie details
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

    //Отправим запрос контент-ресолверу за нужным курсором
    //Request a content resolver for a needed cursor
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
