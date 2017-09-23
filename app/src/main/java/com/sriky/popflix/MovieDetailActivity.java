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
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.squareup.picasso.Picasso;
import com.sriky.popflix.data.MoviesContract.MoviesEntry;
import com.sriky.popflix.databinding.ActivityMovieDetailBinding;
import com.sriky.popflix.loaders.FetchMovieDataTaskLoader;
import com.sriky.popflix.parcelables.MovieTrailer;
import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MovieDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String>,
        FetchMovieDataTaskLoader.FetchMovieDataTaskListener {

    private static final String TAG = MovieDetailActivity.class.getSimpleName();

    /* the projection array used to query data from the movies tables */
    private static final String[] MOVIE_DETAILS_PROJECTION = {
            MoviesEntry.MOVIE_TITLE,
            MoviesEntry.MOVIE_RELEASE_DATE,
            MoviesEntry.MOVIE_VOTE_AVERAGE,
            MoviesEntry.MOVIE_POSTER_PATH,
            MoviesEntry.MOVIE_OVERVIEW};

    /* indexes to access the data from the cursor for the projection defined above */
    private static final int INDEX_MOVIE_TITLE = 0;
    private static final int INDEX_MOVIE_RELEASE_DATE = 1;
    private static final int INDEX_MOVIE_VOTE_AVERAGE = 2;
    private static final int INDEX_MOVIE_POSTER_PATH = 3;
    private static final int INDEX_MOVIE_OVERVIEW = 4;

    private ArrayList<MovieTrailer> mMovieTrailersList;

    private ActivityMovieDetailBinding mMovieDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mMovieDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);

        mMovieDetailBinding.tvErrorMsg.setText(
                getString(R.string.data_download_error));

        //setMoviePosterImageHeight();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MovieDataUtils.MOVIE_ID_INTENT_EXTRA_KEY)) {
            /* get the movie ID from the intent passed by PopularMoviesActivity */
            final String movieID = intent.getStringExtra(MovieDataUtils.MOVIE_ID_INTENT_EXTRA_KEY);
            URL url = NetworkUtils.buildVidesURL(movieID, MovieDataUtils.TMDB_API_KEY);
            Bundle loaderBundle = new Bundle();
            loaderBundle.putString(MovieDataUtils.FETCH_MOVIE_DATA_URL_KEY, url.toString());

            /* trigger the FetchMovieDataTaskLoader to download movie data.
             * The following call will initialize a new loader if one doesn't exist.
             * If an old loader exist and has loaded the data, then onLoadFinished() will be triggered. */
            getSupportLoaderManager().initLoader(MovieDataUtils.DETAIL_MOVIE_DATA_LOADER_ID,
                    loaderBundle, MovieDetailActivity.this);

            /* query the local database for the movie records using the movie id. */
            new AsyncTask<Void, Void, Cursor>() {

                @Override
                protected Cursor doInBackground(Void... voids) {
                    return getContentResolver().query(
                            MoviesEntry.CONTENT_URI.buildUpon().appendPath(movieID).build(),
                            MOVIE_DETAILS_PROJECTION,
                            null,
                            null,
                            null);
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    bindViews(cursor);
                }
            }.execute();
        }
    }

    /**
     * Binds the views in this activity with the data from the cursor.
     * @param cursor
     */
    private void bindViews(Cursor cursor) {
        Log.d(TAG, "bindViews: cursor count:" + cursor.getCount());

        /* move the cursor to the correct position */
        cursor.moveToFirst();

        /* set the movie title
        * TODO: a11y support */
        mMovieDetailBinding.tvMovieTitle.setText(cursor.getString(INDEX_MOVIE_TITLE));

        /* set thumbnail
         * TODO: a11y support
         */
        String relativePath = cursor.getString(INDEX_MOVIE_POSTER_PATH);
        Uri uri = NetworkUtils.getURLForImageWithRelativePathAndSize(relativePath,
                MovieDataUtils.getQueryThumbnailWidthPath());
        Picasso.with(this)
                .load(uri)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(mMovieDetailBinding.thumbnailWithDetails.thumbnailView);

        /* set release date
         * TODO: a11y support
         */
        String formattedYear = cursor.getString(INDEX_MOVIE_RELEASE_DATE);
        formattedYear = formattedYear.substring(0, formattedYear.indexOf("-"));
        mMovieDetailBinding.thumbnailWithDetails.tvDate.setText(formattedYear);

        /* set ratings
         * TODO: a11y support
         */
        double ratings = cursor.getDouble(INDEX_MOVIE_VOTE_AVERAGE);
        Log.d(TAG, "bindViews: ratings: " + ratings);
        mMovieDetailBinding.thumbnailWithDetails.tvRatings.setText(
                String.format(getString(R.string.format_ratings), ratings));

        /* overview */
        mMovieDetailBinding.tvOverview.setText(cursor.getString(INDEX_MOVIE_OVERVIEW));
    }

    /**
     * Sets the height of the imageview for the movie poster thumbnail to half of the screen height.
     */
    private void setMoviePosterImageHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mMovieDetailBinding.thumbnailWithDetails.thumbnailView.getLayoutParams().height = size.y / 2;
    }

    /**
     * Method to set appreciate data to all the views once data was downloaded successfully.
     */
    private void onDownloadSuccess() {
        //hide the progress bar.
        mMovieDetailBinding.progressBar.setVisibility(View.INVISIBLE);
        //hide error msg tv.
        mMovieDetailBinding.tvErrorMsg.setVisibility(View.INVISIBLE);

        for (MovieTrailer movieTrailer : mMovieTrailersList) {
            //m

        }

    }

    private void onFetchFailed() {
        Log.d(TAG, "onFetchFailed()");
        //hide the progress bar.
        mMovieDetailBinding.progressBar.setVisibility(View.INVISIBLE);
        mMovieDetailBinding.tvErrorMsg.setVisibility(View.VISIBLE);
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
        mMovieDetailBinding.progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: id = " + id);
        return new FetchMovieDataTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null) {
            Log.d(TAG, "onLoadFinished: queryResult.length() = " + data.length());
            mMovieTrailersList = MovieDataUtils.getMovieTrailers(data);
            onDownloadSuccess();
        } else {
            onFetchFailed();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
