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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.sriky.popflix.R;
import com.sriky.popflix.data.MoviesContract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provides utility methods to initialize and trigger a background {@link MovieDataSyncIntentService}
 * to fetch movie data from the API.
 */

public class MovieDataSyncUtils {
    /* constants used to define the execution window for the jobs */
    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    /* List to keep track of triggering the fetch service */
    private static List<String> sInitializedQueryList = new ArrayList<>();

    /**
     * Schedules a {@link Job} to query movies data.
     *
     * @param context   Context that will be passed to other methods and used to access the
     *                  ContentResolver.
     * @param queryPath The API path to query.
     */
    private static void scheduleFirebaseFetchJob(Context context, String queryPath) {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

        Job fetchMovieDataJob = firebaseJobDispatcher.newJobBuilder()
                /* setting the unique tag so the job can be identified */
                .setTag(queryPath)
                /* setting the constraints to perform the job only on Wifi.*/
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                /* setting the execution window for the job anywhere from 3hours to 4hours */
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                /* since we need the data to be updated regularly, it should be a recurring job */
                .setRecurring(true)
                /* the service to perform the job */
                .setService(MovieDataFirebaseJobService.class)
                .setLifetime(Lifetime.FOREVER)
                /* if the job with the specified tag already exists then a new one will be created */
                .setReplaceCurrent(true)
                .build();

        firebaseJobDispatcher.schedule(fetchMovieDataJob);
    }

    /**
     * Initializes and starts the {@link MovieDataSyncIntentService} if that hasn't been started already.
     *
     * @param context   Context that will be passed to other methods and used to access the
     *                  ContentResolver.
     * @param queryPath The API path to query.
     */
    synchronized public static void initialize(final Context context, final String queryPath) {
        if (sInitializedQueryList.contains(queryPath)) return;

        sInitializedQueryList.add(queryPath);

        /* initiate the job that will periodically fetch the latest data. */
        scheduleFirebaseFetchJob(context, queryPath);

        final String selection;

        if (queryPath.equals(context.getString(R.string.sort_order_popular))) {
            selection = MoviesContract.MoviesEntry.POPULAR + " =? ";
        } else if (queryPath.equals(context.getString(R.string.sort_order_top_rated))) {
            selection = MoviesContract.MoviesEntry.TOP_RATED + " =? ";
        } else {
            throw new RuntimeException("Unsupported queryPath: " + queryPath);
        }

        /* check if the data exists in the local database. If not trigger the service to fetch data */
        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] projectionColumns = {MoviesContract.MoviesEntry._ID};
                Cursor cursor = context.getContentResolver().query(
                        MoviesContract.MoviesEntry.CONTENT_URI,
                        projectionColumns,
                        selection,
                        new String[]{"1"},
                        null);
                if (cursor == null || cursor.getCount() == 0) {
                    fetchDataImmediately(context, queryPath);
                }
                cursor.close();
            }
        });

        checkForEmpty.start();
    }

    /**
     * Immediately starts the {@link MovieDataSyncIntentService} to fetch data.
     *
     * @param context   The Context used to start the IntentService for the sync.
     * @param queryPath The API path to query.
     */
    public static void fetchDataImmediately(Context context, String queryPath) {
        Intent fetchMovieDataIntent = new Intent(context, MovieDataSyncIntentService.class);
        fetchMovieDataIntent.putExtra(MovieDataSyncIntentService.QUERY_PATH_BUNDLE_KEY, queryPath);
        context.startService(fetchMovieDataIntent);
    }
}
