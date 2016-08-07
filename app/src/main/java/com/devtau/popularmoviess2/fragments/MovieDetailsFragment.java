package com.devtau.popularmoviess2.fragments;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.activities.MovieDetailsActivity;
import com.devtau.popularmoviess2.activities.MainActivity;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.databinding.FragmentMovieDetailsBinding;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.utility.Logger;
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
    private FragmentMovieDetailsBinding binding;

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
        //https://developer.android.com/topic/libraries/data-binding/index.html
        //https://stfalcon.com/en/blog/post/faster-android-apps-with-databinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie_details, container, false);
        return binding.getRoot();
    }



    //LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_RESULTS:
                if(movieId != 0) {
                    return new CursorLoader(getContext(), MoviesTable.buildOrderUri(movieId), null, null, null, null);
                }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_RESULTS:
                data.moveToFirst();
                movie = new Movie(data);
                binding.setMovie(movie);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Logger.d(LOG_TAG, "onLoaderReset()");
    }
}
