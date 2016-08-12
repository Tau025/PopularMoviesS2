package com.devtau.popularmoviess2.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.devtau.popularmoviess2.model.Review;
import com.devtau.popularmoviess2.services.DownloaderService;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.view.ReviewsListViewInterface;
import java.util.ArrayList;
/**
 * Презентер, показывающий список рецензий по выбранному фильму
 * Presenter for the selected movie reviews list
 */
public class ReviewsListPresenter {
    private static final String LOG_TAG = ReviewsListPresenter.class.getSimpleName();
    private ReviewsListViewInterface view;
    private long movieId;
    private BroadcastReceiver myBroadcastReceiver;

    public ReviewsListPresenter(ReviewsListViewInterface view, long movieId) {
        this.view = view;
        this.movieId = movieId;
        myBroadcastReceiver = initBroadCastReceiver();
    }

    private BroadcastReceiver initBroadCastReceiver() {
        return new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                //На выполнение всего метода у нас есть 10 секунд, иначе появится ANR
                //To finish all operations in this method we have 10 seconds before ANR
                if(intent.getAction() == null || !Constants.MY_BROADCAST_ACTION.equals(intent.getAction())
                        || !intent.hasExtra(Constants.REVIEWS_LIST_EXTRA)) return;
                ArrayList<Review> reviewsList = intent.getParcelableArrayListExtra(Constants.REVIEWS_LIST_EXTRA);
                if(reviewsList != null) {
                    Logger.v(LOG_TAG, "reviewsList.size(): " + String.valueOf(reviewsList.size()));
                } else {
                    Logger.v(LOG_TAG, "reviewsList is null");
                }
                view.populateList(reviewsList);
            }
        };
    }

    public void downloadReviews() {
        DownloaderService.download(view.getContext(), movieId, DownloaderService.ACTION_DOWNLOAD_REVIEWS);
        registerBroadCastReceiver();
    }

    public void registerBroadCastReceiver() {
        //возобновим регистрацию широковещательного приёмника всякий раз при разворачивании приложения
        view.getContext().registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.MY_BROADCAST_ACTION));
    }

    public void unregisterBroadCastReceiver() {
        //отменим регистрацию широковещательного приёмника всякий раз при сворачивании приложения
        view.getContext().unregisterReceiver(myBroadcastReceiver);
    }
}
