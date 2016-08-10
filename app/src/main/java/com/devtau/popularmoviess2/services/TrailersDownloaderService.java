package com.devtau.popularmoviess2.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.devtau.popularmoviess2.model.Trailer;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
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
import java.util.ArrayList;
/**
 * Сервис для асинхронной загрузки списка трейлеров с нашего сервера
 * Service for asynchronous downloading trailers list from server
 */
public class TrailersDownloaderService extends IntentService {
    private static final String ACTION_DOWNLOAD_TRAILERS = "com.devtau.popularmoviess2.utility.action.DOWNLOAD_TRAILERS";
    private static final String EXTRA_MOVIE_ID = "com.devtau.popularmoviess2.utility.extra.MOVIE_ID";
    private static final String LOG_TAG = TrailersDownloaderService.class.getSimpleName();

    public TrailersDownloaderService() {
        super("ImageDownloaderService");
    }

    public static void downloadTrailers(Context context, long movieId) {
        Intent intent = new Intent(context, TrailersDownloaderService.class);
        intent.setAction(ACTION_DOWNLOAD_TRAILERS);
        intent.putExtra(EXTRA_MOVIE_ID, movieId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_MOVIE_ID)
                && ACTION_DOWNLOAD_TRAILERS.equals(intent.getAction())) {
            long movieId = intent.getLongExtra(EXTRA_MOVIE_ID, 0);
            Logger.v(LOG_TAG, "Started TrailersDownloaderService");

            URL trailersEndpoint = getUrlFromMovieId(movieId);
            Logger.d(LOG_TAG, "trailersEndpoint: " + String.valueOf(trailersEndpoint));
            if(trailersEndpoint == null) {
                Logger.e(LOG_TAG, "Can't start download with null URL");
                return;
            }

            String jsonString = requestJSONStringFromServer(trailersEndpoint);
            Logger.v(LOG_TAG, "jsonString: " + String.valueOf(jsonString));
            if (TextUtils.isEmpty(jsonString) || "".equals(jsonString)) {
                Logger.e(LOG_TAG, "Can't parse not valid jsonString");
                return;
            }

            ArrayList<Trailer> trailersList = parseTrailersListJSON(jsonString);
            if(trailersList == null) {
                Logger.e(LOG_TAG, "trailersList is null. Terminating service");
                return;
            }

            sendBroadcast(trailersList);
        }
    }


    @Nullable
    private URL getUrlFromMovieId(long movieId) {
        String trailersEndpoint;
        if (movieId != 0) {
            Uri.Builder builder = new Uri.Builder();
            //        https://api.themoviedb.org/3/movie/550?api_key=###&append_to_response=releases,trailers
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(String.valueOf(movieId))
                    .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY_VALUE)
                    .appendQueryParameter("append_to_response", "trailers");

            trailersEndpoint = builder.build().toString();
        } else {
            Logger.e(LOG_TAG, "movieId not valid");
            trailersEndpoint = "";
        }

        try {
            return new URL(trailersEndpoint);
        } catch (MalformedURLException e) {
            Logger.e(LOG_TAG, "Couldn't transform trailersEndpoint from String to URL", e);
            return null;
        }
    }

    @Nullable
    private String requestJSONStringFromServer(URL requestUrl) {
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

    //Метод распарсит JSON строку и вернет List трейлеров, перечисленных в предоставленной JSON строке
    //Parses JSON string to a List of Trailers stated in provided string
    @Nullable
    private ArrayList<Trailer> parseTrailersListJSON(String JSONString) {
        try {
            JSONObject serverAnswer = new JSONObject(JSONString);
            JSONObject trailersJSONObject = serverAnswer.getJSONObject(Constants.JSON_TRAILERS);
            JSONArray trailersJSONArray = trailersJSONObject.getJSONArray(Constants.JSON_YOUTUBE);

            ArrayList<Trailer> trailersList = new ArrayList<>();
            for (int i = 0; i < trailersJSONArray.length(); i++) {
                JSONObject JSONTrailer = trailersJSONArray.getJSONObject(i);
                String source = JSONTrailer.getString(Constants.JSON_TRAILER_SOURCE);
                String name = JSONTrailer.getString(Constants.JSON_TRAILER_NAME);
                String size = JSONTrailer.getString(Constants.JSON_TRAILER_SIZE);
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

    private void sendBroadcast(ArrayList<Trailer> trailersList) {
        Intent intent = new Intent(Constants.MY_BROADCAST_ACTION)
                .putParcelableArrayListExtra(Constants.TRAILERS_LIST_EXTRA, trailersList);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }
}
