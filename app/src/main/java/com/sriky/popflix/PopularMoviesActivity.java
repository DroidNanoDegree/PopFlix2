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

package com.sriky.popflix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sriky.popflix.adaptors.PopularMoviesAdaptor;
import com.sriky.popflix.data.MoviesContract;
import com.sriky.popflix.loaders.FetchMovieDataTaskLoader;
import com.sriky.popflix.settings.SettingsActivity;
import com.sriky.popflix.sync.MovieDataSyncUtils;
import com.sriky.popflix.utilities.MovieDataUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main Activity that is launched from the Launcher.
 * Responsible for querying TMDB's APIs and displaying movies in the specified order.
 */
public class PopularMoviesActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        PopularMoviesAdaptor.MoviePosterOnClickEventListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        FetchMovieDataTaskLoader.FetchMovieDataTaskListener {

    private static final String TAG = PopularMoviesActivity.class.getSimpleName();

    /* the amount time to wait prior to displaying an error message if data isn't loader by the
     * time(millis) specified here.
     */
    private static final long DATA_LOAD_TIMEOUT_LIMIT = 30000;
    private static final long COUNT_DOWN_INTERVAL = 1000;

    /*
     * Handle to the RecyclerView to aid in reset the list when user toggles btw
     * most_popular and top_rated movies from the settings menu.
     */
    @BindView(R.id.rv_posters)
    RecyclerView mMoviePostersRecyclerView;

    @BindView(R.id.pb_popularMoviesActivity)
    ProgressBar mProgressBar;

    @BindView(R.id.tv_error_msg)
    TextView mErrorMessageTextView;

    //handle to the adaptor instance.
    private PopularMoviesAdaptor mPopularMoviesAdaptor;

    //bool to keep track of change in preferences to display sort order for the movies.
    private boolean mDisplaySortingOrderChanged;

    /* recyclerView position */
    private int mPosition = RecyclerView.NO_POSITION;

    /* CountDownTimer used to issue a timeout when data doesn't load within the specified time. */
    private CountDownTimer mDataFetchTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);
        ButterKnife.bind(this);

        mMoviePostersRecyclerView.setHasFixedSize(true);

        mPopularMoviesAdaptor = new PopularMoviesAdaptor(this, this);
        mMoviePostersRecyclerView.setAdapter(mPopularMoviesAdaptor);

        showProgressBarAndHideErrorMessage();

        setSortingOrderFromSharedPreferences();

        /* trigger the asynctaskloader to download movie data.
         * The following call will initialize a new loader if one doesn't exist.
         * If an old loader exist and has loaded the data, then onLoadFinished() will be triggered.*/
        getSupportLoaderManager().initLoader(MovieDataUtils.BASIC_MOVIE_DATA_LOADER_ID,
                null, PopularMoviesActivity.this);

        /* initialize the sync service if it isn't running already */
        MovieDataSyncUtils.initialize(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDisplaySortingOrderChanged) {
            MovieDataSyncUtils.fetchDataImmediately(PopularMoviesActivity.this);
            mDisplaySortingOrderChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.sort_order_key))) {
            mDisplaySortingOrderChanged = true;
        }
    }

    @Override
    public void onClickedMovieId(int movieId) {
        try {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDataUtils.MOVIE_ID_INTENT_EXTRA_KEY, Integer.toString(movieId));
            startActivity(intent);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPreExecute() {
        Log.d(TAG, "onPreExecute: ()");
        showProgressBarAndHideErrorMessage();
    }

    /**
     * Called by {@link LoaderManager} when new loader needs to be created.
     *
     * @param id   The ID of the loader that needs to start.
     * @param args The bundle containing data for the loader.
     * @return CursorLoader pointing to the movies table.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: id = " + id);

        switch (id) {
            case MovieDataUtils.BASIC_MOVIE_DATA_LOADER_ID: {
                return new CursorLoader(this,
                        MoviesContract.MoviesEntry.CONTENT_URI,
                        MovieDataUtils.MOVIE_DATA_PROJECTION,
                        null,
                        null,
                        null);
            }

            default:
                throw new RuntimeException("Loader not implemented id:" + id);

        }
    }

    /**
     * This call is triggered when the CursorLoader loads data from the database or when the
     * data in the database gets updated.
     *
     * @param loader The cursor loader.
     * @param data   The cursor to the movies tables.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: data.length() = " + data.getCount());
        mPopularMoviesAdaptor.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mMoviePostersRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() > 0) {
            onDataLoadComplete();
        } else {
            /* will there is no data set up a countdown to display an error message in case
             * the data doesn't load in the specified time.
             */
            mDataFetchTimer = new CountDownTimer(DATA_LOAD_TIMEOUT_LIMIT, COUNT_DOWN_INTERVAL) {

                public void onTick(long millisUntilFinished) {
                    Log.i(TAG, "waiting on data " + millisUntilFinished / COUNT_DOWN_INTERVAL +
                            "secs remaining for timeout:!");
                }

                public void onFinish() {
                    onDataLoadFailed();
                }
            };
            mDataFetchTimer.start();
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /* since the loader data is invalid we should clear the adaptor that is pointing to that data */
        mPopularMoviesAdaptor.swapCursor(null);
    }

    /**
     * Sets the mSortingOrder variable to appropriate value from sharedPreference, if any.
     * Otherwise "popular" will be set as the default.
     */
    private void setSortingOrderFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Displays the progress bar and hides the error message views.
     */
    private void showProgressBarAndHideErrorMessage() {
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the progress bar view and makes the the error message view VISIBLE.
     */
    private void hideProgressBarAndShowErrorMessage() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    /**
     * On successfully downloading data from the API
     */
    private void onDataLoadComplete() {
        Log.d(TAG, "onDataLoadComplete()");
        /* hide the progress bar & the error msg view. */
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        /* if the timer was running then cancel it. */
        if (mDataFetchTimer != null) {
            mDataFetchTimer.cancel();
            mDataFetchTimer = null;
        }
    }

    /**
     * If there were issues downloading data from TMDB, then hide the progress bar view and
     * display an error message to the user.
     */
    private void onDataLoadFailed() {
        Log.d(TAG, "onDataLoadFailed()");
        hideProgressBarAndShowErrorMessage();
    }
}
