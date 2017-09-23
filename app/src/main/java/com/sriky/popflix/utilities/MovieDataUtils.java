/*
 * Copyright (C) 2017 Srikanth Basappa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.sriky.popflix.utilities;

import android.content.ContentValues;
import android.util.Log;

import com.sriky.popflix.BuildConfig;
import com.sriky.popflix.data.MoviesContract.MoviesEntry;
import com.sriky.popflix.parcelables.MovieTrailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Helper class to handle json parsing, setting the query path for movie thumbnails etc.
 */
public final class MovieDataUtils {

    public static final String MOVIE_ID_INTENT_EXTRA_KEY = "movie_id";
    public static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;

    //async task loader IDs.
    public static final int BASIC_MOVIE_DATA_LOADER_ID = 100;
    public static final int DETAIL_MOVIE_DATA_LOADER_ID = BASIC_MOVIE_DATA_LOADER_ID + 1;

    //URL keys for sending and retrieving urls with the "FetchMovieDataTaskLoader".
    public static final String FETCH_MOVIE_DATA_URL_KEY = "fetch_movie_data_url";

    /* project array used to query data from movies table. */
    public static final String[] MOVIE_DATA_PROJECTION = {
            MoviesEntry.MOVIE_ID,
            MoviesEntry.MOVIE_POSTER_PATH,
    };

    /* indexes for get the column data from the cursor. */
    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_POSTER_PATH = 1;

    private static final String TAG = MovieDataUtils.class.getSimpleName();

    //TMDB query - json keys.
    private static final String JSON_KEY_ARRAY_RESULTS = "results";
    private static final String JSON_KEY_MOVIE_POSTER_PATH = "poster_path";
    private static final String JSON_KEY_MOVIE_OVERVIEW = "overview";
    private static final String JSON_KEY_MOVIE_VOTE_COUNT = "vote_count";
    private static final String JSON_KEY_MOVIE_VOTE_AVERAGE = "vote_average";
    private static final String JSON_KEY_MOVIE_ID = "id";
    private static final String JSON_KEY_MOVIE_TITLE = "title";
    private static final String JSON_KEY_MOVIE_RELEASE_DATE = "release_date";
    private static final String JSON_KEY_MOVIE_VIDEO_ID = "id";
    private static final String JSON_KEY_MOVIE_VIDEO_KEY = "key";
    private static final String JSON_KEY_MOVIE_VIDEO_NAME = "name";
    private static final String JSON_KEY_MOVIE_VIDEO_SITE = "site";
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
     * Generates an ArrayList of MovieTrailer objects from the responseJSON from the API.
     * @param responseJSON The JSON response from the API.
     * @return Array List of MovieTrailer objects.
     */
    public static ArrayList<MovieTrailer> getMovieTrailers(String responseJSON) {
        ArrayList<MovieTrailer> movieTrailers = new ArrayList<>();
        try {
            //validate the response from the server.
            if (isResponseValid(responseJSON)) {
                JSONObject moviesData = new JSONObject(responseJSON);
                JSONArray jsonArrayResults = moviesData.getJSONArray(JSON_KEY_ARRAY_RESULTS);
                if (jsonArrayResults != null) {
                    for (int i = 0; i < jsonArrayResults.length(); i++) {
                        JSONObject data = (JSONObject) jsonArrayResults.get(i);
                        MovieTrailer movieTrailer = new MovieTrailer(
                                data.getString(JSON_KEY_MOVIE_VIDEO_ID),
                                data.getString(JSON_KEY_MOVIE_VIDEO_KEY),
                                data.getString(JSON_KEY_MOVIE_VIDEO_NAME),
                                data.getString(JSON_KEY_MOVIE_VIDEO_SITE));
                        movieTrailers.add(movieTrailer);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieTrailers;
    }

    /**
     * Builds the {@link ContentValues} Array from the JSON response string.
     *
     * @param queryResult The JSON response string from the API.
     * @return {@link ContentValues} Array. Caller to expect and handle null for the return.
     */
    public static ContentValues[] buildContentValuesArrayfromJSONResponse(String queryResult) {
        try {
            //validate the response from the server.
            if (isResponseValid(queryResult)) {
                JSONObject moviesData = new JSONObject(queryResult);
                JSONArray jsonArrayResults = moviesData.getJSONArray(JSON_KEY_ARRAY_RESULTS);
                if (jsonArrayResults != null) {
                    ContentValues[] contentValuesArray = new ContentValues[jsonArrayResults.length()];
                    for (int i = 0; i < jsonArrayResults.length(); i++) {
                        JSONObject data = (JSONObject) jsonArrayResults.get(i);
                        ContentValues cv = new ContentValues();
                        cv.put(MoviesEntry.MOVIE_ID, data.getInt(JSON_KEY_MOVIE_ID));
                        cv.put(MoviesEntry.MOVIE_TITLE, data.getString(JSON_KEY_MOVIE_TITLE));
                        cv.put(MoviesEntry.MOVIE_POSTER_PATH, data.getString(JSON_KEY_MOVIE_POSTER_PATH));
                        cv.put(MoviesEntry.MOVIE_OVERVIEW, data.getString(JSON_KEY_MOVIE_OVERVIEW));
                        cv.put(MoviesEntry.MOVIE_RELEASE_DATE, data.getString(JSON_KEY_MOVIE_RELEASE_DATE));
                        cv.put(MoviesEntry.MOVIE_VOTE_AVERAGE, data.getDouble(JSON_KEY_MOVIE_VOTE_AVERAGE));
                        cv.put(MoviesEntry.MOVIE_VOTE_COUNT, data.getInt(JSON_KEY_MOVIE_VOTE_COUNT));
                        contentValuesArray[i] = cv;
                    }
                    return contentValuesArray;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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
            Log.e(TAG, "isResponseValid: Bad Response from server, where status code = "
                    + statusCode + ", status message = " + statusMsg);
            return false;
        }
        return true;
    }
}
