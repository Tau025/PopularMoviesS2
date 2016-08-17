package com.devtau.popularmoviess2.model;

import android.database.Cursor;
import android.provider.BaseColumns;
import com.devtau.popularmoviess2.utility.Logger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import static com.devtau.popularmoviess2.database.MoviesTable.*;
/**
 * Главный класс модели всего приложения. Содержит в себе все подробности по фильму
 * Main class of application model. It contains all the details on the movie
 */
public class Movie {
    private final String LOG_TAG = Movie.class.getSimpleName();
    private long id;
    private String title;
    private String posterPath;
    private Calendar posterCacheDate = Calendar.getInstance();
    private String plotSynopsis;
    private double userRating;
    private double popularity;
    private Calendar releaseDate = new GregorianCalendar(1970, 0, 1);
    private boolean isFavorite;


    public Movie(long id, String title, String posterPath, Calendar posterCacheDate, String plotSynopsis,
                 double userRating, double popularity, Calendar releaseDate) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.posterCacheDate = posterCacheDate;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.popularity = popularity;
        this.releaseDate = releaseDate;
    }

    public Movie(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        title = cursor.getString(cursor.getColumnIndex(TITLE));
        posterPath = cursor.getString(cursor.getColumnIndex(POSTER_PATH));
        posterCacheDate.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(POSTER_CACHE_DATE)));
        plotSynopsis = cursor.getString(cursor.getColumnIndex(PLOT_SYNOPSIS));
        userRating = cursor.getDouble(cursor.getColumnIndex(USER_RATING));
        popularity = cursor.getDouble(cursor.getColumnIndex(POPULARITY));
        releaseDate.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(RELEASE_DATE)));
        isFavorite = (cursor.getInt(cursor.getColumnIndex(IS_FAVORITE)) == 1);
    }

    //setter
    public void setPosterCacheDate(Calendar posterCacheDate) {
        this.posterCacheDate = posterCacheDate;
    }

    public void setIsFavorite(boolean favorite) {
        isFavorite = favorite;
        Logger.d(LOG_TAG, "isFavorite: " + String.valueOf(isFavorite));
    }


    //getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public Calendar getPosterCacheDate() {
        return posterCacheDate;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public double getUserRating() {
        return userRating;
    }

    public double getPopularity() {
        return popularity;
    }

    public Calendar getReleaseDate() {
        return releaseDate;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public String getReleaseYear() {
        Calendar defaultDate = new GregorianCalendar(1970, 0, 1);
        if(releaseDate.compareTo(defaultDate) == 0) {
            return "---";
        } else {
            return String.valueOf(releaseDate.get(Calendar.YEAR));
        }
    }

    public void updateFields(Movie movie) {
        if(movie != null) {
            int fieldsUpdated = 0;
            if(!"".equals(movie.title)) {
                title = movie.title;
                fieldsUpdated++;
            }
            if(!"".equals(movie.posterPath)) {
                posterPath = movie.posterPath;
                fieldsUpdated++;
            }
            if(!"".equals(movie.plotSynopsis)) {
                plotSynopsis = movie.plotSynopsis;
                fieldsUpdated++;
            }
            if(0 != movie.userRating) {
                userRating = movie.userRating;
                fieldsUpdated++;
            }
            if(0 != movie.popularity) {
                popularity = movie.popularity;
                fieldsUpdated++;
            }
            if(null != movie.releaseDate) {
                releaseDate = movie.releaseDate;
                fieldsUpdated++;
            }
            //Мы не хотим обновлять поля isFavorite и posterCacheDate, т.к. они не имеют отношения
            //к базе на сервере и каждый раз при синхронизации с сервера будет приходить фильм
            //с таким же id, но этих полей. Следовательно они будут сбрасываться, а мы этого не хотим.
            //We don't want to update isFavorite & posterCacheDate fields because if we do,
            //SyncAdapter would erase saved isFavorite state every time it syncs with server.
            if(fieldsUpdated < 6) {
                Logger.d(LOG_TAG, "updateFields() finished. fieldsUpdated: " + String.valueOf(fieldsUpdated));
            }
        } else {
            Logger.e(LOG_TAG, "updateFields() cannot receive null movie");
        }
    }

    public String getFormattedUserRating() {
        return String.valueOf(userRating) + " / " + 10;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass() || id == -1) return false;
        Movie that = (Movie) obj;
        if (that.id == -1) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id != -1 ? 31 * id : 0);
    }
}
