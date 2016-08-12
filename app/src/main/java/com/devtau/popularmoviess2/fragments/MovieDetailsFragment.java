package com.devtau.popularmoviess2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.activities.MovieDetailsActivity;
import com.devtau.popularmoviess2.activities.MainActivity;
import com.devtau.popularmoviess2.adapters.TrailersListAdapter;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.model.Trailer;
import com.devtau.popularmoviess2.presenters.MovieDetailsPresenter;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.utility.NetworkHelper;
import com.devtau.popularmoviess2.utility.Utility;
import com.devtau.popularmoviess2.view.MovieDetailsViewInterface;
import java.util.ArrayList;
/**
 * Фрагмент, показывающий подробности по выбранному фильму.
 * Он может отображаться на отдельном экране или вместе со списком фильмов
 *
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailsActivity} on handsets.
 */
public class MovieDetailsFragment extends Fragment implements
        MovieDetailsViewInterface,
        View.OnClickListener{
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

    private TextView tv_title, tv_release_date, tv_user_rating, tv_plot_synopsis;
    private ImageView iv_poster;
    private ToggleButton btn_is_favorite;
    private ListView trailersListView;

    private MovieDetailsPresenter presenter;

    public MovieDetailsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(Constants.MOVIE_ID_EXTRA)) {
            long movieId = getArguments().getLong(Constants.MOVIE_ID_EXTRA);
            presenter = new MovieDetailsPresenter(this, movieId);

        } else {
            Logger.e(LOG_TAG, "MOVIE_ID_EXTRA not found");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        //Поскольку мы используем ListView с хедером, перед тем, как искать ссылки на отдельные вью,
        //хедер нужно надуть. Для быстрого отображения только хедера, пока лист трейлеров еще не загрузился
        //мы инициируем ListView пустым ArrayList, и переназначаем его после загрузки трейлеров
        //Hence we use ListView with header, we have to inflate header before we can access links
        //to it's children views. In order to speed up loading of whole fragment, first we add
        //an empty ArrayList which will be replaced on trailers list load finished
        trailersListView = (ListView) rootView.findViewById(R.id.movieDetailsListView);;
        trailersListView.addHeaderView(inflater.inflate(R.layout.fragment_movie_details_header, (ViewGroup) rootView, false));
        trailersListView.addFooterView(inflater.inflate(R.layout.fragment_movie_details_footer, (ViewGroup) rootView, false));
        initControls(rootView);
        populateTrailersList(new ArrayList<Trailer>());

        return rootView;
    }

    private void initControls(View rootView) {
        tv_title = (TextView) rootView.findViewById(R.id.tv_title);
        tv_release_date = (TextView) rootView.findViewById(R.id.tv_release_date);
        tv_user_rating = (TextView) rootView.findViewById(R.id.tv_user_rating);
        tv_plot_synopsis = (TextView) rootView.findViewById(R.id.tv_plot_synopsis);
        iv_poster = (ImageView) rootView.findViewById(R.id.iv_poster);
        btn_is_favorite = (ToggleButton) rootView.findViewById(R.id.btn_is_favorite);
        Button btn_show_reviews = (Button) rootView.findViewById(R.id.btn_show_reviews);

        if(btn_is_favorite != null && btn_show_reviews != null) {
            btn_is_favorite.setOnClickListener(this);
            btn_show_reviews.setOnClickListener(this);
        }
    }

    @Override
    public void populateTrailersList(ArrayList<Trailer> trailersList) {
        if(trailersListView == null) return;
        trailersListView.setAdapter(new TrailersListAdapter(getContext(), R.layout.list_item_trailers, trailersList));
        trailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String trailerSource = ((Trailer) adapterView.getItemAtPosition(position)).getSource();
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                        NetworkHelper.getTrailerYoutubeUri(trailerSource)));
            }
        });
    }


    @Override
    public void showMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void populateUI(@NonNull Movie movie) {
        if(tv_title == null || tv_release_date == null || tv_user_rating == null
                || tv_plot_synopsis == null || iv_poster == null || btn_is_favorite == null) return;

        tv_title.setText(movie.getTitle());
        tv_release_date.setText(movie.getReleaseYear());
        tv_user_rating.setText(movie.getFormattedUserRating());
        tv_plot_synopsis.setText(movie.getPlotSynopsis());
        Utility.setPosterImage(iv_poster, movie.getPosterPath());
        btn_is_favorite.setChecked(movie.isFavorite());
    }

    @Override
    public void onStart() {
        super.onStart();
        //возобновим регистрацию широковещательного приёмника всякий раз при разворачивании приложения
        //register BroadCastReceiver every time fragment becomes visible
        presenter.registerBroadCastReceiver();
        presenter.restartLoader();
        presenter.downloadTrailers();
    }

    @Override
    public void onStop() {
        super.onStop();
        //отменим регистрацию широковещательного приёмника всякий раз при сворачивании приложения
        //unregister BroadCastReceiver every time fragment becomes not visible
        presenter.unregisterBroadCastReceiver();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_is_favorite:
                presenter.onFavoriteClick();
                break;
            case R.id.btn_show_reviews:
                presenter.onReviewsClick(getActivity().getSupportFragmentManager());
                break;
        }
    }
}
