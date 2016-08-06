package com.devtau.popularmoviess2.util;
import com.devtau.popularmoviess2.model.SortBy;

public abstract class Constants {
    public static final String IMAGE_STORAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    //    posterSize: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    public static final String POSTER_SIZE = "w500";
    public static final int DEFAULT_POSTER_WIDTH = 200;
    public static final int DEFAULT_POSTER_HEIGHT = 300;
    public static final SortBy DEFAULT_SORT_BY = SortBy.MOST_POPULAR;

    // These are the names of the JSON objects that need to be extracted.
    public static final String RESULTS = "results";
    public static final String ID = "id";
    public static final String TITLE = "original_title";
    public static final String POSTER_PATH = "poster_path";
    public static final String PLOT_SYNOPSIS = "overview";
    public static final String USER_RATING = "vote_average";
    public static final String RELEASE_DATE = "release_date";

    public static final String API_KEY_PARAM = "api_key";
    public static final String API_KEY_VALUE = "dfd949b3dbeb097ef26ee09ef7299615";
}
