package com.devtau.popularmoviess2.utility;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * Сервис для асинхронной загрузки изображений с нашего сервера в папку кэша на устройстве
 * Service for asynchronous downloading images from server to the device cache
 */
public class ImageDownloaderService extends IntentService {
    private static final String ACTION_DOWNLOAD_IMAGE = "com.devtau.popularmoviess2.utility.action.DOWNLOAD_IMAGE";
    private static final String EXTRA_POSTER_PATH = "com.devtau.popularmoviess2.utility.extra.POSTER_PATH";
    private static final String LOG_TAG = ImageDownloaderService.class.getSimpleName();

    public ImageDownloaderService() {
        super("ImageDownloaderService");
    }

    public static void downloadImage(Context context, String posterPath) {
        Intent intent = new Intent(context, ImageDownloaderService.class);
        intent.setAction(ACTION_DOWNLOAD_IMAGE);
        intent.putExtra(EXTRA_POSTER_PATH, posterPath);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_POSTER_PATH)
                && ACTION_DOWNLOAD_IMAGE.equals(intent.getAction())) {
            String posterPath = intent.getStringExtra(EXTRA_POSTER_PATH);
            Bitmap bitmap = handleActionDownloadImage(posterPath);
            if(bitmap != null) {
                FileManager.saveImageToCache(this, posterPath, bitmap);
            }
        }
    }

    @Nullable
    private Bitmap handleActionDownloadImage(String posterPath) {
//        Logger.v(LOG_TAG, "Started ImageDownloaderService");
        InputStream inputStream = null;
        try {
            URL url = getUrlFromPosterPath(posterPath);
            if(url != null) {
                inputStream = url.openConnection().getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            } else {
                Logger.e(LOG_TAG, "Can't start download with null URL");
                return null;
            }
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error opening url connection", e);
            return null;
        } finally {
            try {
                if(inputStream != null) {
//                    Logger.v(LOG_TAG, "Closing inputStream");
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.e(LOG_TAG, "While closing inputStream", e);
            }
        }
    }

    @Nullable
    private URL getUrlFromPosterPath(String posterPath) {
        String posterEndpoint;
        if (TextUtils.isEmpty(posterPath) || "".equals(posterPath)) {
            Logger.e(LOG_TAG, "No valid posterPath found in movie. Replacing with kitty to keep user happy");
            posterEndpoint = "http://kogteto4ka.ru/wp-content/uploads/2012/04/%D0%9A%D0%BE%D1%82%D0%B5%D0%BD%D0%BE%D0%BA.jpg";
        } else {
            posterEndpoint = Constants.IMAGE_STORAGE_ON_SERVER_BASE_URL + Constants.POSTER_SIZE + posterPath;
        }

        try {
            return new URL(posterEndpoint);
        } catch (MalformedURLException e) {
            Logger.e(LOG_TAG, "Couldn't transform posterEndpoint from String to URL", e);
            return null;
        }
    }
}
