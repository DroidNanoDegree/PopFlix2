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

package com.sriky.popflix.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.sriky.popflix.R;
import com.sriky.popflix.data.MoviesContract.MoviesEntry;
import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs various data syncing tasks.
 */

public class MovieDataSyncTask {

    private static final String TAG = MovieDataSyncTask.class.getSimpleName();

    private static List<String> mFavoritedMovieIdList = new ArrayList<>();

    /**
     * Task to fetch movie data from the API
     *
     * @param context    Used to get the {@link ContentResolver}
     * @param queryPath  The API path to query.
     */
    synchronized public static void fetchMovieData(Context context, String queryPath) {
        /* build the url for the query. */
        String urlStr = NetworkUtils.buildURL(queryPath, MovieDataUtils.TMDB_API_KEY).toString();
        Log.d(TAG, "loadInBackground: URL = " + urlStr);
        try {
            URL url = new URL(urlStr);
            String responseJSON = NetworkUtils.getStringResponseFromHttpUrl(url);
            ContentResolver contentResolver = context.getContentResolver();

            /* cache the favorited movieIDs */
            cacheFavoritedMovies(contentResolver);

            /* clear old entries */
            clearOldEntries(context, contentResolver, queryPath);

            /* generate content values from the json response for bulk insert into the movies table */
            ContentValues[] valuesForBulkInsertion = MovieDataUtils
                    .buildContentValuesArrayfromJSONResponse(context, responseJSON, queryPath);

             /* add the new data in the movies table. */
            contentResolver.bulkInsert(MoviesEntry.CONTENT_URI, valuesForBulkInsertion);

            /* updated favorites flag in the table for the movieIDs that was cached */
            updatedFavorites(contentResolver);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Caches the favorited movieIDs.
     *
     * @param contentResolver The contentResolver used to query the movies table.
     */
    private static void cacheFavoritedMovies(ContentResolver contentResolver) {
        mFavoritedMovieIdList.clear();
        Cursor cursor = contentResolver.query(MoviesEntry.CONTENT_URI,
                MovieDataUtils.FAVORITE_MOVIE_DATA_PROJECTION,
                MoviesEntry.USER_FAVOURITE + " =? ",
                new String[]{"1"},
                null);
        while (cursor.moveToNext()) {
            mFavoritedMovieIdList.add(cursor.getString(MovieDataUtils.INDEX_MOVIE_ID));
        }
        cursor.close();
    }

    /**
     * Clear the old entries in the movies table that match the queryPath.
     *
     * @param queryPath       The API path from where the data was queried.
     * @param contentResolver The contentResolver used to query the movies table.
     */
    private static void clearOldEntries(Context context, ContentResolver contentResolver,
                                        String queryPath) {
        String selection = null;

        if (queryPath.equals(context.getString(R.string.sort_order_popular))) {
            selection = MoviesEntry.POPULAR + " =? ";
        } else if (queryPath.equals(context.getString(R.string.sort_order_top_rated))) {
            selection = MoviesEntry.TOP_RATED + " =? ";
        }

        /* delete old entries in the table */
        contentResolver.delete(MoviesEntry.CONTENT_URI,
                selection,
                new String[]{"1"});
    }

    /**
     * Update the favorite column for the cached movieIDs that were cahched.
     *
     * @param contentResolver The contentResolver used to query the movies table.
     */
    private static void updatedFavorites(ContentResolver contentResolver) {
        for (String movieId : mFavoritedMovieIdList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MoviesEntry.USER_FAVOURITE, 1);
            int numRecordsUpdated = contentResolver.update(MoviesEntry.buildMovieUriWithId(movieId),
                    contentValues,
                    null,
                    null);

            Log.d(TAG, "updatedFavorites() numRecordsUpdated : " + numRecordsUpdated
                    + " for movieId : " + movieId);
        }
    }
}
