package com.devtau.popularmoviess2.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.activities.MovieDetailsActivity;
import com.devtau.popularmoviess2.activities.MainActivity;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.presenters.MovieDetailsPresenter;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.utility.Utility;
import com.devtau.popularmoviess2.view.MovieDetailsViewInterface;
/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailsActivity}
 * on handsets.
 */
public class MovieDetailsFragment extends Fragment implements
        MovieDetailsViewInterface {
    public static final String MOVIE_ID_EXTRA = "movieIdExtra";
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

    private TextView tv_title, tv_release_date, tv_user_rating, tv_plot_synopsis;
    private ImageView iv_poster;
    private ToggleButton btn_is_favorite;

    private MovieDetailsPresenter presenter;

    public MovieDetailsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(MOVIE_ID_EXTRA)) {
            long movieId = getArguments().getLong(MOVIE_ID_EXTRA);
            presenter = new MovieDetailsPresenter(this, movieId);
            presenter.restartLoader();
        } else {
            Logger.e(LOG_TAG, "MOVIE_ID_EXTRA not found");
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
                presenter.onFavoriteClick();
            }
        });
    }


    @Override
    public void showMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void populateUI(@NonNull Movie movie) {
        tv_title.setText(movie.getTitle());
        tv_release_date.setText(movie.getReleaseYear());
        tv_user_rating.setText(movie.getFormattedUserRating());
        tv_plot_synopsis.setText(movie.getPlotSynopsis());
        Utility.setPosterImage(iv_poster, movie.getPosterPath());
        btn_is_favorite.setChecked(movie.isFavorite());
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
