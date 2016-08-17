package com.devtau.popularmoviess2.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.devtau.popularmoviess2.model.Review;
import com.devtau.popularmoviess2.model.Trailer;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.utility.NetworkHelper;
import java.net.URL;
import java.util.ArrayList;
/**
 * Сервис для асинхронной загрузки списка трейлеров или рецензий с нашего сервера
 * Service for asynchronous downloading trailers or reviews list from server
 */
public class DownloaderService extends IntentService {
    public static final String ACTION_DOWNLOAD_TRAILERS = "com.devtau.popularmoviess2.services.action.DOWNLOAD_TRAILERS";
    public static final String ACTION_DOWNLOAD_REVIEWS = "com.devtau.popularmoviess2.services.action.DOWNLOAD_REVIEWS";
    private static final String LOG_TAG = DownloaderService.class.getSimpleName();

    public DownloaderService() {
        super("DownloaderService");
    }

    public static void download(Context context, long movieId, String action) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(action);
        intent.putExtra(Constants.MOVIE_ID_EXTRA, movieId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(Constants.MOVIE_ID_EXTRA)) return;

        long movieId = intent.getLongExtra(Constants.MOVIE_ID_EXTRA, 0);

        if (ACTION_DOWNLOAD_TRAILERS.equals(intent.getAction())) {
            Logger.v(LOG_TAG, "Started DownloaderService");

            URL trailersEndpoint = NetworkHelper.getTrailersUrl(movieId);
            Logger.d(LOG_TAG, "trailersEndpoint: " + String.valueOf(trailersEndpoint));
            if(trailersEndpoint == null) {
                Logger.e(LOG_TAG, "Can't start download with null URL");
                return;
            }

            String jsonString = NetworkHelper.requestJSONStringFromServer(trailersEndpoint);
            Logger.v(LOG_TAG, "jsonString: " + String.valueOf(jsonString));
            if (TextUtils.isEmpty(jsonString) || "".equals(jsonString)) {
                Logger.e(LOG_TAG, "Can't parse not valid jsonString");
                return;
            }

            ArrayList<Trailer> trailersList = NetworkHelper.parseTrailersListJSON(jsonString);
            if(trailersList == null) {
                Logger.e(LOG_TAG, "trailersList is null. Terminating service");
                return;
            }

            sendTrailersBroadcast(trailersList);
        }

        if (ACTION_DOWNLOAD_REVIEWS.equals(intent.getAction())) {
            Logger.v(LOG_TAG, "Started DownloaderService");

            URL reviewsEndpoint = NetworkHelper.getReviewsUrl(movieId);
            Logger.d(LOG_TAG, "reviewsEndpoint: " + String.valueOf(reviewsEndpoint));
            if(reviewsEndpoint == null) {
                Logger.e(LOG_TAG, "Can't start download with null URL");
                return;
            }

            String jsonString = NetworkHelper.requestJSONStringFromServer(reviewsEndpoint);
            Logger.v(LOG_TAG, "jsonString: " + String.valueOf(jsonString));
            if (TextUtils.isEmpty(jsonString) || "".equals(jsonString)) {
                Logger.e(LOG_TAG, "Can't parse not valid jsonString");
                return;
            }

            ArrayList<Review> reviewsList = NetworkHelper.parseReviewsListJSON(jsonString);
            if(reviewsList == null) {
                Logger.e(LOG_TAG, "reviewsList is null. Terminating service");
                return;
            }

            sendReviewsBroadcast(reviewsList);
        }
    }

    private void sendTrailersBroadcast(ArrayList<Trailer> trailersList) {
        Intent intent = new Intent(Constants.MY_BROADCAST_ACTION)
                .putParcelableArrayListExtra(Constants.TRAILERS_LIST_EXTRA, trailersList);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }

    private void sendReviewsBroadcast(ArrayList<Review> reviewsList) {
        Intent intent = new Intent(Constants.MY_BROADCAST_ACTION)
                .putParcelableArrayListExtra(Constants.REVIEWS_LIST_EXTRA, reviewsList);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }
}
