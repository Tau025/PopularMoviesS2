package com.devtau.popularmoviess2.model;

import android.content.Context;
import android.support.annotation.NonNull;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.utility.Constants;
/**
 * Перечисление для вариантов сортировки списка фильмов
 * Enum for sort options of the list of movies
 */
public enum SortBy {
    MOST_POPULAR(R.string.sort_order_most_popular, R.string.sort_order_most_popular_key, MoviesTable.POPULARITY),
    TOP_RATED(R.string.sort_order_top_rated, R.string.sort_order_top_rated_key, MoviesTable.USER_RATING);

    private final int captionID;
    private final int keyID;
    private final String databaseID;

    SortBy(int captionID, int keyID, String databaseID) {
        this.captionID = captionID;
        this.keyID = keyID;
        this.databaseID = databaseID;
    }

    @NonNull
    public String getDescription(@NonNull Context context) {
        return context.getString(captionID);
    }

    @NonNull
    public String getKeyID(@NonNull Context context) {
        return context.getString(keyID);
    }

    @NonNull
    public String getDatabaseID() {
        return databaseID;
    }

    public static SortBy toggle(SortBy sortBy) {
        switch (sortBy) {
            case MOST_POPULAR:
                return SortBy.TOP_RATED;
            case TOP_RATED:
                return SortBy.MOST_POPULAR;
            default:
                return Constants.DEFAULT_SORT_BY;
        }
    }
}
