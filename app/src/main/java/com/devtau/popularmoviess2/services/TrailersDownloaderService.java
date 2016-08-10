package com.devtau.popularmoviess2.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.devtau.popularmoviess2.model.Trailer;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
import com.devtau.popularmoviess2.utility.NetworkHelper;
import java.net.URL;
import java.util.ArrayList;
/**
 * Сервис для асинхронной загрузки списка трейлеров с нашего сервера
 * Service for asynchronous downloading trailers list from server
 */
public class TrailersDownloaderService extends IntentService {
    private static final String ACTION_DOWNLOAD_TRAILERS = "com.devtau.popularmoviess2.utility.action.DOWNLOAD_TRAILERS";
    private static final String EXTRA_MOVIE_ID = "com.devtau.popularmoviess2.utility.extra.MOVIE_ID";
    private static final String LOG_TAG = TrailersDownloaderService.class.getSimpleName();

    public TrailersDownloaderService() {
        super("ImageDownloaderService");
    }

    public static void downloadTrailers(Context context, long movieId) {
        Intent intent = new Intent(context, TrailersDownloaderService.class);
        intent.setAction(ACTION_DOWNLOAD_TRAILERS);
        intent.putExtra(EXTRA_MOVIE_ID, movieId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_MOVIE_ID)
                && ACTION_DOWNLOAD_TRAILERS.equals(intent.getAction())) {
            long movieId = intent.getLongExtra(EXTRA_MOVIE_ID, 0);
            Logger.v(LOG_TAG, "Started TrailersDownloaderService");

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

            sendBroadcast(trailersList);
        }
    }

    private void sendBroadcast(ArrayList<Trailer> trailersList) {
        Intent intent = new Intent(Constants.MY_BROADCAST_ACTION)
                .putParcelableArrayListExtra(Constants.TRAILERS_LIST_EXTRA, trailersList);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }
}
