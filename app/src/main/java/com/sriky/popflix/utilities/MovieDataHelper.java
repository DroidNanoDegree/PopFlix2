package com.sriky.popflix.utilities;

import android.util.Log;

import com.sriky.popflix.BuildConfig;
import com.sriky.popflix.MovieData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Helper class to handle json parsing, setting the query path for movie thumbnails etc.
 */
public final class MovieDataHelper {

    public static final String MOVIE_ID_INTENT_EXTRA_KEY = "movie_id";
    public static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;

    //async task loader IDs.
    public static final int BASIC_MOVIE_DATA_LOADER_ID = 100;
    public static final int DETAIL_MOVIE_DATA_LOADER_ID = BASIC_MOVIE_DATA_LOADER_ID + 1;

    //URL keys for sending and retrieving urls with the "FetchMovieDataTaskLoader".
    public static final String FETCH_MOVIE_DATA_URL_KEY = "fetch_movie_data_url";

    private static final String TAG = MovieDataHelper.class.getSimpleName();

    //TMDB query - json keys.
    private static final String JSON_KEY_ARRAY_RESULTS = "results";
    private static final String JSON_KEY_MOVIE_POSTER_PATH = "poster_path";
    private static final String JSON_KEY_MOVIE_OVERVIEW = "overview";
    private static final String JSON_KEY_MOVIE_VOTE_AVERAGE = "vote_average";
    private static final String JSON_KEY_MOVIE_ID = "id";
    private static final String JSON_KEY_MOVIE_TITLE = "title";
    private static final String JSON_KEY_MOVIE_RELEASE_DATE = "release_date";
    private static final String JSON_KEY_STATUS_MESSAGE = "status_message";
    private static final String JSON_KEY_STATUS_CODE = "status_code";

    //the Uri path that determine the width of the poster thumbnail.
    private static String sQueryThumbnailWidthPath = "w185";//default.;

    /**
     * Returns the closest possible width query path supported by TMDB.
     *
     * @param thumbnailWidth - desired width to display movie posters.
     */
    public static void setThumbnailQueryPath(int thumbnailWidth) {
        Log.d(TAG, "setThumbnailQueryPath: thumbnailWidth = " + thumbnailWidth);
        if (thumbnailWidth <= 0) {
            Log.w(TAG, "setThumbnailQueryPath: thumbnailWidth = " + thumbnailWidth + " in incorrect, will use default w185!");
        }

        if (thumbnailWidth > 0 && thumbnailWidth <= 92) {
            sQueryThumbnailWidthPath = "w92";
        } else if (thumbnailWidth > 92 && thumbnailWidth <= 154) {
            sQueryThumbnailWidthPath = "w154";
        } else if (thumbnailWidth > 154 && thumbnailWidth <= 185) {
            sQueryThumbnailWidthPath = "w185";
        } else if (thumbnailWidth > 185 && thumbnailWidth <= 342) {
            sQueryThumbnailWidthPath = "w342";
        } else if (thumbnailWidth > 342 && thumbnailWidth <= 500) {
            sQueryThumbnailWidthPath = "w500";
        } else if (thumbnailWidth > 500) {
            sQueryThumbnailWidthPath = "w780";
        }
    }

    /**
     * The support width path calculated based on the width of the screen and number of gird columns.
     *
     * @return path to the query url that specifies the width supported by TMDB.
     */
    public static String getQueryThumbnailWidthPath() {
        return sQueryThumbnailWidthPath;
    }

    /**
     * This method will generate a MovieData object from the response json after querying TMDB API
     * for a particular movie's details.
     *
     * @param queryResult movie details response from TMDB API for a particular movie.
     * @return MovieData object from response JSON string.
     */
    public static MovieData getMovieDataFrom(String queryResult) {
        MovieData movieData = new MovieData();
        try {
            //validate the response from the server.
            if (isResponseValid(queryResult)) {
                JSONObject movieDetails = new JSONObject(queryResult);
                movieData.setPosterPath(movieDetails.getString(JSON_KEY_MOVIE_POSTER_PATH));
                movieData.setOverview(movieDetails.getString(JSON_KEY_MOVIE_OVERVIEW));
                movieData.setTitle(movieDetails.getString(JSON_KEY_MOVIE_TITLE));
                movieData.setReleaseDate(movieDetails.getString(JSON_KEY_MOVIE_RELEASE_DATE));
                movieData.setVoteAverage(movieDetails.getString(JSON_KEY_MOVIE_VOTE_AVERAGE));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieData;
    }

    /**
     * Generates a list of MovieData objects from the response json, after querying TMDB API
     * for movies organised in a specific order(eg: "popular" or "top_rated".
     *
     * @param queryResult response json, after querying TMDB API
     *                    for movies organised in a specific order(eg: "popular" or "top_rated".
     * @return ArrayList of MovieData objects.
     */
    public static ArrayList<MovieData> getListfromJSONResponse(String queryResult) {
        ArrayList<MovieData> movieDataList = new ArrayList<>();
        try {
            //validate the response from the server.
            if (isResponseValid(queryResult)) {
                JSONObject moviesData = new JSONObject(queryResult);
                JSONArray jsonArrayResults = moviesData.getJSONArray(JSON_KEY_ARRAY_RESULTS);
                if (jsonArrayResults != null) {
                    for (int i = 0; i < jsonArrayResults.length(); i++) {
                        JSONObject data = (JSONObject) jsonArrayResults.get(i);
                        MovieData movieData = new MovieData();
                        movieData.setPosterPath(data.getString(JSON_KEY_MOVIE_POSTER_PATH));
                        movieData.setMovieID(data.getString(JSON_KEY_MOVIE_ID));
                        movieDataList.add(movieData);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieDataList;
    }

    /**
     * Validates the response from the TMDB server.
     *
     * @param response Query response String.
     * @return TRUE if there were no errors contained in the response.
     * @throws JSONException when the JSON is malformed.
     */
    private static boolean isResponseValid(String response) throws JSONException {
        if ((response.contains(JSON_KEY_STATUS_CODE)) ||
                (response.contains(JSON_KEY_STATUS_MESSAGE))) {
            JSONObject responseObject = new JSONObject(response);
            String statusCode = responseObject.getString(JSON_KEY_STATUS_CODE);
            String statusMsg = responseObject.getString(JSON_KEY_STATUS_MESSAGE);
            Log.e(TAG, "isResponseValid: Bad Response from server, where status code = " + statusCode + ", status message = " + statusMsg);
            return false;
        }
        return true;
    }
}
