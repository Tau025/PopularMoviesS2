package com.devtau.popularmoviess2.util;

import com.devtau.popularmoviess2.model.SortBy;

public abstract class Constants {
    public static final String IMAGE_STORAGE_ON_SERVER_BASE_URL = "http://image.tmdb.org/t/p/";
    //    posterSize: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    //Nexus 4 с экраном 768*1280 обладает пиксельной плотностью 2.0 и w400 будет картинкой пиксель-в-пиксель
    //Nexus 4 with it's 768*1280 screen has screen density of 2.0 and w400 would be pixel-perfect
    public static final String POSTER_SIZE = "w500";
    public static final SortBy DEFAULT_SORT_BY = SortBy.MOST_POPULAR;
    public static final String MY_BROADCAST_ACTION = "com.devtau.popularmoviess2.SENT_BROADCAST";
    public static final String TRAILERS_LIST_EXTRA = "TRAILERS_LIST_EXTRA";
    public static final String REVIEWS_LIST_EXTRA = "REVIEWS_LIST_EXTRA";
    public static final String YOUTUBE_TRAILER_THUMB = "http://img.youtube.com/vi/$/0.jpg";

    //Имена JSON объектов, которые мы извлекаем из ответа сервера themoviedb
    //These are the names of the JSON objects that need to be extracted.
    public static final String JSON_RESULTS = "results";
    public static final String JSON_ID = "id";
    public static final String JSON_TITLE = "original_title";
    public static final String JSON_POSTER_PATH = "poster_path";
    public static final String JSON_PLOT_SYNOPSIS = "overview";
    public static final String JSON_USER_RATING = "vote_average";
    public static final String JSON_POPULARITY = "popularity";
    public static final String JSON_RELEASE_DATE = "release_date";

    public static final String JSON_TRAILER_NAME = "name";
    public static final String JSON_TRAILER_SIZE = "size";
    public static final String JSON_TRAILER_SOURCE = "key";
    public static final String JSON_TRAILER_TYPE = "type";

    public static final String JSON_REVIEW_AUTHOR = "author";
    public static final String JSON_REVIEW_CONTENT = "content";

    //    /data/data/com.devtau.popularmoviess2/ImageCache/?????????????.jpg
    public static final String IMAGES_CACHE_DIR_NAME = "ImageCache";
    public static final String CACHED_IMAGE_EXTENSION = ".jpg";
    public static final int CASHED_IMAGE_LIFETIME = 10 * 60 * 1000;//10 min

    //Максимальное количество попыток переподключения и лаг между ними при отсутствующем интернете
    //Maximum reconnection retry count and time lag between them if there is no internet connection
    public static final int RETRY_COUNT = 6;
    public static final int RETRY_LAG = 500;//ms

    //Временной лаг между синхронизациями с сервером и допустимая погрешность сдвига в секундах
    //Interval at which to sync with server in seconds
    public static final int SYNC_INTERVAL = 60 * 180;//3 hours
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public static final String MOVIE_ID_EXTRA = "movieIdExtra";
    public static final String API_KEY_PARAM = "api_key";
    public static final String API_KEY_VALUE = "dfd949b3dbeb097ef26ee09ef7299615";
}