package com.devtau.popularmoviess2.model;

import android.content.Context;
import com.devtau.popularmoviess2.R;

public enum SortBy {
    MOST_POPULAR(R.string.sort_order_most_popular, R.string.sort_order_most_popular_key),
    TOP_RATED(R.string.sort_order_top_rated, R.string.sort_order_top_rated_key);

    private final int captionID;
    private final int keyID;

    SortBy(int captionID, int keyID) {
        this.captionID = captionID;
        this.keyID = keyID;
    }

    public String getDescription(Context context) {
        return context.getString(captionID);
    }

    public String getKeyID(Context context) {
        if(context != null) {
            return context.getString(keyID);
        } else {
            return "";
        }
    }
}
