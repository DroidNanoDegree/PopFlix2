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
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String>,
        FetchMovieDataTaskLoader.FetchMovieDataTaskListener {

    private static final String TAG = MovieDetailActivity.class.getSimpleName();
    private static final String PARCEL_KEY = "movie_data";

    @BindView(R.id.pb_details_activity)
    ProgressBar mProgressBar;

    @BindView(R.id.tv_details_activity_error_msg)
    TextView mErrorMessageTextView;

    @BindView(R.id.iv_details_thumbnail)
    ImageView mMoviePosterImageView;

    @BindView(R.id.tv_movie_title)
    TextView mMovieTitleTextView;

    @BindView(R.id.tv_release_date)
    TextView mReleaseDateTextView;

    @BindView(R.id.tv_overview)
    TextView mOverviewTextView;

    @BindView(R.id.rb_ratings)
    RatingBar mRatingsBar;

    private MovieData mMovieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        mErrorMessageTextView.setText(getString(R.string.data_download_error));

        setMoviePosterImageHeight();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MovieDataUtils.MOVIE_ID_INTENT_EXTRA_KEY)) {
            String movieID = intent.getStringExtra(MovieDataUtils.MOVIE_ID_INTENT_EXTRA_KEY);
            URL url = NetworkUtils.buildURL(movieID, MovieDataUtils.TMDB_API_KEY);
            Bundle loaderBundle = new Bundle();
            loaderBundle.putString(MovieDataUtils.FETCH_MOVIE_DATA_URL_KEY, url.toString());
            //trigger the asynctaskloader to download movie data.
            //The following call will initialize a new loader if one doesn't exist.
            //If an old loader exist and has loaded the data, then onLoadFinished() will be triggered.
            getSupportLoaderManager().initLoader(MovieDataUtils.DETAIL_MOVIE_DATA_LOADER_ID,
                    loaderBundle, MovieDetailActivity.this);
        }
    }

    /**
     * Sets the height of the imageview for the movie poster thumbnail to half of the screen height.
     */
    private void setMoviePosterImageHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mMoviePosterImageView.getLayoutParams().height = size.y / 2;
    }

    /**
     * Method to set appreciate data to all the views once data was downloaded successfully.
     */
    private void onDownloadSuccess() {
        //hide the progress bar.
        mProgressBar.setVisibility(View.INVISIBLE);
        //hide error msg tv.
        mErrorMessageTextView.setVisibility(View.INVISIBLE);

        String relativePath = mMovieData.getPosterPath();
        Uri uri = NetworkUtils.getURLForImageWithRelativePathAndSize(relativePath,
                MovieDataUtils.getQueryThumbnailWidthPath());
        Picasso.with(this)
                .load(uri)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(mMoviePosterImageView);

        mReleaseDateTextView.setText(mMovieData.getReleaseDate());
        mOverviewTextView.setText(mMovieData.getOverview());
        mMovieTitleTextView.setText(mMovieData.getTitle());
        try {
            float ratings = Float.parseFloat(mMovieData.getVoteAverage()) / 2;
            mRatingsBar.setRating(ratings);
        } catch (NumberFormatException e) {
            //hide the ratings bar if there was an exception.
            mRatingsBar.setVisibility(View.INVISIBLE);
            e.printStackTrace();
        }
    }

    private void onFetchFailed() {
        Log.d(TAG, "onFetchFailed()");
        //hide the progress bar.
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPreExecute() {
        Log.d(TAG, "onPreExecute: ()");
        //show the progress bar.
        mProgressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: id = "+id);
        return new FetchMovieDataTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null) {
            Log.d(TAG, "onLoadFinished: queryResult.length() = " + data.length());
            mMovieData = MovieDataUtils.getMovieDataFrom(data);
            onDownloadSuccess();
        } else {
            onFetchFailed();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
