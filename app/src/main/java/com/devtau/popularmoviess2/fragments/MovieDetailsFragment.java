package com.devtau.popularmoviess2.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.activities.MovieDetailsActivity;
import com.devtau.popularmoviess2.activities.MainActivity;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.utility.Utility;
/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailsActivity}
 * on handsets.
 */
public class MovieDetailsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String MOVIE_ID_EXTRA = "movieIdExtra";
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private long movieId;
    private Movie movie;
    private static final int LOADER_RESULTS = 556847;
    private TextView tv_title, tv_release_date, tv_user_rating, tv_plot_synopsis;
    private ImageView iv_poster;
    private ToggleButton btn_is_favorite;

    public MovieDetailsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(MOVIE_ID_EXTRA)) {
            movieId = getArguments().getLong(MOVIE_ID_EXTRA);
            getActivity().getSupportLoaderManager().restartLoader(LOADER_RESULTS, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        initControls(view);
        return view;
    }

    private void initControls(View view) {
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_release_date = (TextView) view.findViewById(R.id.tv_release_date);
        tv_user_rating = (TextView) view.findViewById(R.id.tv_user_rating);
        tv_plot_synopsis = (TextView) view.findViewById(R.id.tv_plot_synopsis);
        iv_poster = (ImageView) view.findViewById(R.id.iv_poster);
        btn_is_favorite = (ToggleButton) view.findViewById(R.id.btn_is_favorite);

        btn_is_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movie.setIsFavorite(!movie.isFavorite());
                getActivity().getContentResolver().update(MoviesTable.CONTENT_URI,
                        MoviesTable.getContentValues(movie),
                        BaseColumns._ID + "=?", new String[]{String.valueOf(movieId)});
            }
        });
    }

    private void populateUI() {
        if(movie == null) return;

        tv_title.setText(movie.getTitle());
        tv_release_date.setText(movie.getReleaseYear());
        tv_user_rating.setText(movie.getFormattedUserRating());
        tv_plot_synopsis.setText(movie.getPlotSynopsis());
        Utility.setPosterImage(iv_poster, movie.getPosterPath());
        btn_is_favorite.setChecked(movie.isFavorite());
    }

    //LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_RESULTS:
                if(movieId != 0) {
                    return new CursorLoader(getContext(), MoviesTable.buildMovieUri(movieId), null, null, null, null);
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
                data.moveToFirst();
                movie = new Movie(data);
                populateUI();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Logger.d(LOG_TAG, "onLoaderReset()");
    }
}
