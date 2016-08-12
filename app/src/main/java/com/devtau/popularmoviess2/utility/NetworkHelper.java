package com.devtau.popularmoviess2.utility;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.model.Review;
import com.devtau.popularmoviess2.model.SortBy;
import com.devtau.popularmoviess2.model.Trailer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
/**
 * Хелпер для всех операций работы с сетью: формирование URL, получение и парсинг ответа от сервера
 * Helper class for all network operations such as creating URLs, downloading and parsing operations
 */
public class NetworkHelper {
    private static final String LOG_TAG = NetworkHelper.class.getSimpleName();

    @Nullable
    public static URL getMoviesListUrl(Context context, SortBy sortBy) {
        if (sortBy == null) {
            Logger.e(LOG_TAG, "sortBy not valid");
            return null;
        }

        //Подготовим URL для запроса к серверу themoviedb.org
        //Construct the URL for themoviedb.org query
        String sortByString = sortBy.getKeyID(context);
        Uri.Builder builder = new Uri.Builder();
//        http://api.themoviedb.org/3/movie/popular?api_key=dfd949b3dbeb097ef26ee09ef7299615
        builder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(sortByString)
                .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY_VALUE);
        try {
            return new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            Logger.e(LOG_TAG, "Couldn't transform moviesListEndpoint from String to URL", e);
            return null;
        }
    }


    @Nullable
    public static URL getTrailersUrl(long movieId) {
        if (movieId == 0) {
            Logger.e(LOG_TAG, "movieId not valid");
            return null;
        }

        //Подготовим URL для запроса к серверу themoviedb.org
        //Construct the URL for themoviedb.org query
        Uri.Builder builder = new Uri.Builder();
        //        https://api.themoviedb.org/3/movie/550?api_key=###&append_to_response=releases,trailers
        builder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(String.valueOf(movieId))
                .appendPath("videos")
                .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY_VALUE);

        try {
            return new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            Logger.e(LOG_TAG, "Couldn't transform trailersEndpoint from String to URL", e);
            return null;
        }
    }


    @Nullable
    public static URL getReviewsUrl(long movieId) {
        if (movieId == 0) {
            Logger.e(LOG_TAG, "movieId not valid");
            return null;
        }

        //Подготовим URL для запроса к серверу themoviedb.org
        //Construct the URL for themoviedb.org query
        Uri.Builder builder = new Uri.Builder();
        //        https://api.themoviedb.org/3/movie/550?api_key=###&append_to_response=releases,trailers
        builder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(String.valueOf(movieId))
                .appendPath("reviews")
                .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY_VALUE);

        try {
            return new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            Logger.e(LOG_TAG, "Couldn't transform trailersEndpoint from String to URL", e);
            return null;
        }
    }



    @Nullable
    public static URL getPosterPathUrl(String posterPath) {
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


    @Nullable
    public static Uri getTrailerYoutubeUri(String trailerSource) {
        if (TextUtils.isEmpty(trailerSource) || "".equals(trailerSource)) {
            Logger.e(LOG_TAG, "trailerSource not valid");
            return null;
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("www.youtube.com")
                .appendPath("watch")
                .appendQueryParameter("v", trailerSource);
        return Uri.parse(builder.build().toString());
    }


    @Nullable
    public static String requestJSONStringFromServer(URL requestUrl) {
        //urlConnection и reader должы объявляться вне try/catch блока,
        //чтобы их можно было закрыть в блоке finally
        //urlConnection & reader need to be declared outside the try/catch
        //so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            //Создадим запрос к серверу themoviedb.org и откроем urlConnection
            //Create the request to themoviedb.org, and open the connection
            urlConnection = (HttpURLConnection) requestUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Создадим inputStream и сохраним его содержимое в строку используя буфер
            //Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                //Нет смысла парсить пустой JSON. Выходим из метода
                //Stream was empty. No point in parsing.
                return null;
            }

            return buffer.toString();

        } catch (IOException e) {
            Logger.e(LOG_TAG, "While opening url connection", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Logger.e(LOG_TAG, "While closing stream", e);
                }
            }
        }
    }


    //Метод распарсит JSON строку и вернет List фильмов, перечисленных в предоставленной JSON строке
    //Parses JSON string to a List of Movies
    @Nullable
    public static List<Movie> parseMoviesListJSON(String JSONString) {
        try {
            JSONObject serverAnswer = new JSONObject(JSONString);
            JSONArray moviesJsonArray = serverAnswer.getJSONArray(Constants.JSON_RESULTS);

            List<Movie> moviesList = new ArrayList<>();
            for(int i = 0; i < moviesJsonArray.length(); i++) {
                JSONObject JSONMovie = moviesJsonArray.getJSONObject(i);
                long movieID = JSONMovie.getLong(Constants.JSON_ID);
                String movieTitle = JSONMovie.getString(Constants.JSON_TITLE);
                String moviePosterPath = JSONMovie.getString(Constants.JSON_POSTER_PATH);
                String moviePlotSynopsis = JSONMovie.getString(Constants.JSON_PLOT_SYNOPSIS);
                double movieUserRating = JSONMovie.getDouble(Constants.JSON_USER_RATING);
                double moviePopularity = JSONMovie.getDouble(Constants.JSON_POPULARITY);

                Calendar movieReleaseDate = new GregorianCalendar();
                try {
                    movieReleaseDate = new GregorianCalendar(1970, 0, 1);
                    String dateString = JSONMovie.getString(Constants.JSON_RELEASE_DATE);
                    movieReleaseDate.setTime(Utility.theMovieDBDateFormat.parse(dateString));
                } catch (ParseException e) {
                    Logger.e(LOG_TAG, "while parsing releaseDate from JSON", e);
                }

                //Соберем из всех подготовленных компонентов объект класса Movie
                //Create a Movie from all fields that we have now
                Movie parsedMovie = new Movie(movieID, movieTitle, moviePosterPath,
                        moviePlotSynopsis, movieUserRating, moviePopularity, movieReleaseDate);
                moviesList.add(parsedMovie);
            }
            return moviesList;
        } catch (JSONException e) {
            Logger.e(LOG_TAG, "While parsing JSON");
            return null;
        }
    }

    //Метод распарсит JSON строку и вернет ArrayList трейлеров, перечисленных в предоставленной JSON строке
    //Parses JSON string to a ArrayList of Trailers stated in provided string
    @Nullable
    public static ArrayList<Trailer> parseTrailersListJSON(String JSONString) {
        try {
            JSONObject serverAnswer = new JSONObject(JSONString);
            JSONArray trailersJSONArray = serverAnswer.getJSONArray(Constants.JSON_RESULTS);

            ArrayList<Trailer> trailersList = new ArrayList<>();
            for (int i = 0; i < trailersJSONArray.length(); i++) {
                JSONObject JSONTrailer = trailersJSONArray.getJSONObject(i);
                String source = JSONTrailer.getString(Constants.JSON_TRAILER_SOURCE);
                String name = JSONTrailer.getString(Constants.JSON_TRAILER_NAME);
                int size = JSONTrailer.getInt(Constants.JSON_TRAILER_SIZE);
                String type = JSONTrailer.getString(Constants.JSON_TRAILER_TYPE);

                //Соберем из всех подготовленных компонентов объект класса Trailer
                //Create a Trailer from all fields that we have now
                Trailer parsedTrailer = new Trailer(source, name, size, type);
                trailersList.add(parsedTrailer);
            }
            return trailersList;
        } catch (JSONException e) {
            Logger.e(LOG_TAG, "While parsing JSON");
            return null;
        }
    }

    //Метод распарсит JSON строку и вернет ArrayList рецензий, перечисленных в предоставленной JSON строке
    //Parses JSON string to a ArrayList of Review stated in provided string
    @Nullable
    public static ArrayList<Review> parseReviewsListJSON(String JSONString) {
        try {
            JSONObject serverAnswer = new JSONObject(JSONString);
            JSONArray reviewsJSONArray = serverAnswer.getJSONArray(Constants.JSON_RESULTS);

            ArrayList<Review> reviewsList = new ArrayList<>();
            for (int i = 0; i < reviewsJSONArray.length(); i++) {
                JSONObject JSONReview = reviewsJSONArray.getJSONObject(i);
                String userName = JSONReview.getString(Constants.JSON_REVIEW_AUTHOR);
                String reviewContent = JSONReview.getString(Constants.JSON_REVIEW_CONTENT);

                //Соберем из всех подготовленных компонентов объект класса Review
                //Create a Review from all fields that we have now
                Review parsedReview = new Review(userName, reviewContent);
                reviewsList.add(parsedReview);
            }
            return reviewsList;
        } catch (JSONException e) {
            Logger.e(LOG_TAG, "While parsing JSON");
            return null;
        }
    }
}