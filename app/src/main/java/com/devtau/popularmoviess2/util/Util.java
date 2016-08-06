package com.devtau.popularmoviess2.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import com.devtau.popularmoviess2.R;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class Util {
    private static final String LOG_TAG = Util.class.getSimpleName();
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static final SimpleDateFormat theMovieDBDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static void loadImageToView(Context context, String imagePath, ImageView target,
                                 int resizeImageWidthTo, int resizeImageHeightTo) {
        if (TextUtils.isEmpty(imagePath) || "".equals(imagePath)) {
            Logger.e(LOG_TAG, "No posterUrlString found in movie. Replacing with Kitty");
            imagePath = "http://kogteto4ka.ru/wp-content/uploads/2012/04/%D0%9A%D0%BE%D1%82%D0%B5%D0%BD%D0%BE%D0%BA.jpg";
        } else {
            imagePath = Constants.IMAGE_STORAGE_BASE_URL + Constants.POSTER_SIZE + imagePath;
        }
        Picasso.with(context)
                .load(imagePath)
                .error(R.drawable.load_failed)
//                .centerCrop()
//                .resize(resizeImageWidthTo, resizeImageHeightTo)
                .into(target);
    }
}
