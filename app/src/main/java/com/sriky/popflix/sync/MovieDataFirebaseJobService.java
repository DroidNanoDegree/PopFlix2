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

import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Job to periodically fetch movie data from the API.
 */

public class MovieDataFirebaseJobService extends JobService {
    private static final String TAG = MovieDataFirebaseJobService.class.getSimpleName();

    /* async task to fetch the movie data in the background */
    private AsyncTask<Void, Void, Void> mfetchMovieDataTask;

    /**
     * Starting point for the job. Contains implementation to offload the work onto to another thread.
     *
     * @return whether there is work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters job) {
        mfetchMovieDataTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG, "doInBackground()");
                MovieDataSyncTask.fetchMovieData(getApplicationContext(), job.getTag());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job, false);
            }
        };
        mfetchMovieDataTask.execute();
        return true;
    }

    /**
     * Called when the job is interrupted. When that happens cancel the job and the system will try
     * and re-start the job.
     *
     * @return whether the job should be retired.
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        if (mfetchMovieDataTask != null) {
            mfetchMovieDataTask.cancel(true);
        }
        return true;
    }
}
