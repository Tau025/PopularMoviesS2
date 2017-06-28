package com.devtau.popularmoviess2.database;

import android.content.UriMatcher;

class CustomUriMatcher extends UriMatcher {
    
    static final int MOVIE = 100;
    static final int MOVIE_BY_ID = 101;
    
    
    CustomUriMatcher() {
        super(UriMatcher.NO_MATCH);
        String authority = MySQLHelper.CONTENT_AUTHORITY;
    
        //Добавьте matcher для каждого шаблона URI, который вы собираетесь использовать
        //For each type of URI you want to add, create a corresponding code.
        addURI(authority, MoviesTable.TABLE_NAME, MOVIE);
        addURI(authority, MoviesTable.TABLE_NAME + "/*", MOVIE_BY_ID);
    }
}
