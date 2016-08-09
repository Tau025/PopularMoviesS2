package com.devtau.popularmoviess2.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;
import com.devtau.popularmoviess2.R;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static final SimpleDateFormat theMovieDBDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    //Использовать библиотеку Picasso было бы слишком просто
    //Picasso is too easy
    public static void loadImageToView(Context context, String imagePath, ImageView target) {
        if (TextUtils.isEmpty(imagePath) || "".equals(imagePath)) {
            Logger.e(LOG_TAG, "No posterUrlString found in movie. Replacing with Kitty");
            imagePath = "http://kogteto4ka.ru/wp-content/uploads/2012/04/%D0%9A%D0%BE%D1%82%D0%B5%D0%BD%D0%BE%D0%BA.jpg";
        } else {
            imagePath = Constants.IMAGE_STORAGE_ON_SERVER_BASE_URL + Constants.POSTER_SIZE + imagePath;
        }

        Picasso.with(context)
                .load(imagePath)
                .error(R.drawable.load_failed)
                .centerCrop()
                .into(target);
    }


    public static void setPosterImage(ImageView imageView, String posterPath) {
        //Проверим, нет ли сохраненной фотографии на устройстве
        //Check for a cached image on the device
        File cachedImage = FileManager.getImageFromCache(imageView.getContext(), posterPath);
        Logger.v(LOG_TAG, "In setPosterImage(). cachedImage is " + ((cachedImage != null) ? "valid" : "null"));

        if(cachedImage != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(cachedImage.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        } else {
            //Запустим запрос к серверу только если на устройстве нужной фотографии нет
            //Launch image downloader only if there is no cached image
            ImageDownloaderService.downloadImage(imageView.getContext(), posterPath);
        }
    }
}
