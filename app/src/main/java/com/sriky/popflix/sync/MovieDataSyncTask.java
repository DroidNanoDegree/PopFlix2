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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sriky.popflix.R;
import com.sriky.popflix.data.MoviesContract.MoviesEntry;
import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Performs various data syncing tasks.
 */

public class MovieDataSyncTask {

    private static final String TAG = MovieDataSyncTask.class.getSimpleName();

    /**
     * Task to fetch movie data from the API
     *
     * @param context Used to get the {@link ContentResolver}
     */
    synchronized public static void fetchMovieData(Context context) {
        /* get the sorting order stored in the default preference */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOder = sharedPreferences.getString(
                context.getString(R.string.sort_order_key),
                context.getString(R.string.default_sort_order));

        /* build the url for the query. */
        String urlStr = NetworkUtils.buildURL(sortOder, MovieDataUtils.TMDB_API_KEY).toString();
        Log.d(TAG, "loadInBackground: URL = " + urlStr);
        try {
            URL url = new URL(urlStr);
            String responseJSON = NetworkUtils.getStringResponseFromHttpUrl(url);
            ContentValues[] valuesToBulkInsert = MovieDataUtils
                    .buildContentValuesArrayfromJSONResponse(responseJSON);

            if (valuesToBulkInsert != null && valuesToBulkInsert.length > 0) {
                ContentResolver contentResolver = context.getContentResolver();
                /* delete old entries in the table */
                contentResolver.delete(MoviesEntry.CONTENT_URI,
                        null,
                        null);
                /* add the new data in the movies table. */
                contentResolver.bulkInsert(MoviesEntry.CONTENT_URI, valuesToBulkInsert);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
