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

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Subclass of {@link IntentService} to fetch movie data in the background
 */

public class MovieDataSyncIntentService extends IntentService {
    public static final String QUERY_PATH_BUNDLE_KEY = "query_path";

    private static final String TAG = MovieDataSyncIntentService.class.getSimpleName();

    public MovieDataSyncIntentService() {
        super("MovieDataSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent()");
        if (intent.hasExtra(QUERY_PATH_BUNDLE_KEY)) {
            String queryPath = intent.getStringExtra(QUERY_PATH_BUNDLE_KEY);
            Log.d(TAG, "onHandleIntent() queryPath : " + queryPath);
            MovieDataSyncTask.fetchMovieData(this, queryPath);
        }
    }
}
