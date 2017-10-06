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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.sriky.popflix.adaptors.DetailsFragmentPagerAdaptor;
import com.sriky.popflix.adaptors.MovieTrailerAdaptor;
import com.sriky.popflix.data.MoviesContract.MoviesEntry;
import com.sriky.popflix.databinding.ActivityMovieDetailBinding;
import com.sriky.popflix.loaders.FetchMovieDataTaskLoader;
import com.sriky.popflix.parcelables.MovieTrailer;
import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String>,
        FetchMovieDataTaskLoader.FetchMovieDataTaskListener,
        MovieTrailerAdaptor.MovieTrailerOnClickedListener {

    private static final String TAG = MovieDetailActivity.class.getSimpleName();

    /* the projection array used to query data from the movies tables */
    private static final String[] MOVIE_DETAILS_PROJECTION = {
            MoviesEntry.MOVIE_TITLE,
            MoviesEntry.MOVIE_RELEASE_DATE,
            MoviesEntry.MOVIE_VOTE_AVERAGE,
            MoviesEntry.MOVIE_POSTER_PATH,
            MoviesEntry.MOVIE_OVERVIEW,
            MoviesEntry.USER_FAVOURITE
    };

    /* indexes to access the data from the cursor for the projection defined above */
    public static final int INDEX_MOVIE_TITLE = 0;
    public static final int INDEX_MOVIE_RELEASE_DATE = 1;
    public static final int INDEX_MOVIE_VOTE_AVERAGE = 2;
    public static final int INDEX_MOVIE_POSTER_PATH = 3;
    public static final int INDEX_MOVIE_OVERVIEW = 4;
    public static final int INDEX_MOVIE_USER_FAVORITE = 5;

    private ArrayList<MovieTrailer> mMovieTrailersList;

    private ActivityMovieDetailBinding mMovieDetailBinding;

    private Cursor mCursor;

    private MovieTrailerAdaptor mMovieTrailerAdaptor;

    private String mMovieId;

    private Toast mToast;

    private boolean mFavorite;

    private DetailsFragmentPagerAdaptor mDetailsFragmentPagerAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mMovieDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);

        mMovieDetailBinding.trailers.progressBar.setVisibility(View.VISIBLE);

        mMovieDetailBinding.trailers.trailerRecyclerView.setHasFixedSize(true);
        mMovieDetailBinding.trailers.trailerRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mMovieTrailerAdaptor = new MovieTrailerAdaptor(this, this);
        mMovieDetailBinding.trailers.trailerRecyclerView.setAdapter(mMovieTrailerAdaptor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MovieDataUtils.MOVIE_ID_INTENT_EXTRA_KEY)) {
            /* get the movie ID from the intent passed by PopularMoviesActivity */
            mMovieId = intent.getStringExtra(MovieDataUtils.MOVIE_ID_INTENT_EXTRA_KEY);
            URL url = NetworkUtils.buildVideosURL(mMovieId, MovieDataUtils.TMDB_API_KEY);
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
                            MoviesEntry.buildMovieUriWithId(mMovieId),
                            MOVIE_DETAILS_PROJECTION,
                            null,
                            null,
                            null);
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    mCursor = cursor;
                    bindViews(cursor);
                }
            }.execute();
        }
    }

    @Override
    protected void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /* set the icon to indicate whether the movie has been favorited by the user or not */
        if (mCursor != null) {
            mFavorite = mCursor.getInt(INDEX_MOVIE_USER_FAVORITE) > 0;
            if (mFavorite) {
                setIcon(menu.findItem(R.id.action_favorite), R.drawable.favorite_selected);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        } else if (item.getItemId() == R.id.action_favorite) {
            mFavorite = !mFavorite;
            if (mFavorite) {
                setIcon(item, R.drawable.favorite_selected);
            } else {
                setIcon(item, R.drawable.favorite_normal);
            }
            updateRecord();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Update the movies table item record to set the boolean.
     */
    private void updateRecord() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesEntry.USER_FAVOURITE, mFavorite);
        int id = getContentResolver().update(
                MoviesEntry.buildMovieUriWithId(mMovieId),
                contentValues,
                null,
                null);

        Log.d(TAG, "updateRecord() id =" + id);
        int formatId =
                (mFavorite) ? R.string.movie_added_to_favorites
                        : R.string.movie_removed_from_favorites;

        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this,
                String.format(getString(formatId),
                        mCursor.getString(INDEX_MOVIE_TITLE)), Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void setIcon(MenuItem item, int resourceId) {
        item.setIcon(resourceId);
    }

    /**
     * Binds the views in this activity with the data from the cursor.
     *
     * @param cursor The cursor containing information about the movie.
     */
    private void bindViews(Cursor cursor) {
        Log.d(TAG, "bindViews: cursor count:" + cursor.getCount());

        /* move the cursor to the correct position */
        if(!cursor.moveToFirst()) {
            throw new RuntimeException("No data loaded in the cursor!");
        }

        /* set the movie title
        * TODO: a11y support */
        String title = cursor.getString(INDEX_MOVIE_TITLE);
        setTitle(title);

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

        /* set the FragmentPagerAdaptor for the ViewPager used to display overview & reviews */
        mDetailsFragmentPagerAdaptor =
                new DetailsFragmentPagerAdaptor(getSupportFragmentManager(), mCursor, mMovieId);
        mMovieDetailBinding.overviewReviews.vpOverviewReviews.setAdapter(mDetailsFragmentPagerAdaptor);

        /* add the tabbed layout to listen to page change notifications so that the ViewPager
         * can be updated accordingly
         */
        mMovieDetailBinding.overviewReviews.vpOverviewReviews.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(
                        mMovieDetailBinding.overviewReviews.tlOverviewReviews));

        /* update the ViewPager to the appropriate page */
        mMovieDetailBinding.overviewReviews.tlOverviewReviews.addOnTabSelectedListener(
                (new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        Log.d(TAG, "onTabSelected: tab.position: " + tab.getPosition());
                        mMovieDetailBinding.overviewReviews.vpOverviewReviews.setCurrentItem(
                                tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        Log.d(TAG, "onTabUnselected: ");
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        Log.d(TAG, "onTabReselected: ");
                    }
                }));

        /* update the page index when page changes so that ViewPager can resize according to the
         * size of the view.
         */
        mMovieDetailBinding.overviewReviews.vpOverviewReviews.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                                               int positionOffsetPixels) {
                        Log.d(TAG, "onPageScrolled: ");
                    }

                    @Override
                    public void onPageSelected(int position) {
                        Log.d(TAG, "onPageSelected: position: " + position);
                        mMovieDetailBinding.overviewReviews.vpOverviewReviews.reMeasureCurrentPage(
                                mMovieDetailBinding.overviewReviews.vpOverviewReviews.getCurrentItem());
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        Log.d(TAG, "onPageScrollStateChanged: ");
                    }
                });

        int selectedTabIdx = mMovieDetailBinding.overviewReviews.vpOverviewReviews.getCurrentItem();
        if (mMovieDetailBinding.overviewReviews.tlOverviewReviews.getSelectedTabPosition()
                != mDetailsFragmentPagerAdaptor.getItemPosition(selectedTabIdx)) {
            mMovieDetailBinding.overviewReviews.tlOverviewReviews.getTabAt(selectedTabIdx).select();
            mMovieDetailBinding.overviewReviews.vpOverviewReviews.reMeasureCurrentPage(
                    mMovieDetailBinding.overviewReviews.vpOverviewReviews.getCurrentItem());
        }
    }

    /**
     * Method to set appreciate data to all the views once data was downloaded successfully.
     */
    private void onDownloadSuccess() {
        //hide the progress bar.
        mMovieDetailBinding.trailers.progressBar.setVisibility(View.INVISIBLE);
        //hide error msg tv.
        mMovieDetailBinding.trailers.tvErrorMsg.setVisibility(View.INVISIBLE);
        /* set the adaptor for the trailers listview */
        mMovieTrailerAdaptor.updateTrailers(mMovieTrailersList);
    }

    private void onFetchFailed() {
        Log.d(TAG, "onFetchFailed()");
        //hide the progress bar.
        mMovieDetailBinding.trailers.progressBar.setVisibility(View.INVISIBLE);
        mMovieDetailBinding.trailers.tvErrorMsg.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPreExecute() {
        Log.d(TAG, "onPreExecute: ()");
        //show the progress bar.
        mMovieDetailBinding.trailers.progressBar.setVisibility(View.VISIBLE);
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
            if(mMovieTrailersList.size() == 0) {
                mMovieDetailBinding.trailers.tvErrorMsg.setText(getString(R.string.no_trailers));
                onFetchFailed();
            } else {
                onDownloadSuccess();
            }
        } else {
            onFetchFailed();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        mMovieTrailerAdaptor.updateTrailers(null);
    }

    /**
     * Launch the trailer via youtube app or a browser.
     *
     * @param trailerKey The video query key.
     */
    @Override
    public void onClicked(String trailerKey) {
        Intent intent = new Intent(Intent.ACTION_VIEW, NetworkUtils.buildYoutubeUri(trailerKey));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
