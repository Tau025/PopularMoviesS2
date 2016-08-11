package com.devtau.popularmoviess2.model;

import android.content.Context;
import android.support.annotation.NonNull;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.utility.Constants;

import java.util.ArrayList;
/**
 * Перечисление для вариантов сортировки списка фильмов
 * Enum for sort options of the list of movies
 */
public enum SortBy {
    MOST_POPULAR(1, R.string.sort_order_most_popular, R.string.sort_order_most_popular_key, MoviesTable.POPULARITY),
    TOP_RATED(2, R.string.sort_order_top_rated, R.string.sort_order_top_rated_key, MoviesTable.USER_RATING),
    //IS_FAVORITE не используется, как параметр запроса к серверу и поэтому нам здесь не нужен keyID
    //IS_FAVORITE is not being used as query param for server request so there is no need for keyID
    IS_FAVORITE(3, R.string.sort_order_favorite, 0, MoviesTable.IS_FAVORITE);

    private final long id;
    private final int titleId;
    private final int keyID;
    private final String databaseID;

    public static final String SORT_BY_EXTRA = "SORT_BY_EXTRA";

    SortBy(int id, int titleId, int keyID, String databaseID) {
        this.id = id;
        this.titleId = titleId;
        this.keyID = keyID;
        this.databaseID = databaseID;
    }

    public long getId() {
        return id;
    }

    @NonNull
    public String getTitle(@NonNull Context context) {
        return context.getString(titleId);
    }

    @NonNull
    public String getKeyID(@NonNull Context context) {
        return context.getString(keyID);
    }

    @NonNull
    public String getDatabaseID() {
        return databaseID;
    }


    public static SortBy getById(long id){
        if(id == MOST_POPULAR.id) return MOST_POPULAR;
        else if(id == TOP_RATED.id) return TOP_RATED;
        else if(id == IS_FAVORITE.id) return IS_FAVORITE;
        else return Constants.DEFAULT_SORT_BY;
    }

    public static ArrayList<SortBy> getAllEnumItems() {
        ArrayList<SortBy> response = new ArrayList<>();
        response.add(MOST_POPULAR);
        response.add(TOP_RATED);
        response.add(IS_FAVORITE);
        return response;
    }
}
