package com.devtau.popularmoviess2.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.adapters.ReviewsRecyclerViewAdapter;
import com.devtau.popularmoviess2.model.Review;
import com.devtau.popularmoviess2.presenters.ReviewsListPresenter;
import com.devtau.popularmoviess2.util.Constants;
import com.devtau.popularmoviess2.util.Logger;
import com.devtau.popularmoviess2.view.ReviewsListViewInterface;
import java.util.ArrayList;
/**
 * Вью-фрагмент, показывающий список рецензий по выбранному фильму
 * View-fragment for the selected movie reviews list
 */
public class ReviewsFragment extends Fragment implements
        ReviewsListViewInterface {
    private static final String LOG_TAG = ReviewsFragment.class.getSimpleName();
    private ReviewsListPresenter presenter;
    private RecyclerView recyclerView;
    private ReviewsRecyclerViewAdapter rvAdapter;

    public static ReviewsFragment newInstance(long movieId) {
        ReviewsFragment reviewsFragment = new ReviewsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(Constants.MOVIE_ID_EXTRA, movieId);
        reviewsFragment.setArguments(arguments);
        return reviewsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(Constants.MOVIE_ID_EXTRA)) {
            long movieId = getArguments().getLong(Constants.MOVIE_ID_EXTRA);
            presenter = new ReviewsListPresenter(this, movieId);
            presenter.downloadReviews();
        } else {
            Logger.e(LOG_TAG, "MOVIE_ID_EXTRA not found");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);;
        return rootView;
    }


    @Override
    public void populateList(ArrayList<Review> reviewsList) {
        rvAdapter = new ReviewsRecyclerViewAdapter(reviewsList);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        //возобновим регистрацию широковещательного приёмника всякий раз при разворачивании приложения
        //register BroadCastReceiver every time fragment becomes visible
        presenter.registerBroadCastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        //отменим регистрацию широковещательного приёмника всякий раз при сворачивании приложения
        //unregister BroadCastReceiver every time fragment becomes not visible
        presenter.unregisterBroadCastReceiver();
    }
}
