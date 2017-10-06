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
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sriky.popflix.adaptors.PopularMoviesAdaptor;
import com.sriky.popflix.data.MoviesContract;
import com.sriky.popflix.databinding.PopularMoviesBinding;
import com.sriky.popflix.loaders.FetchMovieDataTaskLoader;
import com.sriky.popflix.sync.MovieDataSyncUtils;
import com.sriky.popflix.utilities.MovieDataUtils;


/**
 * Fragment Class - contains implementation for triggering TMDB's API query and
 * displaying movie thumbnails/poster. Fragments are initialized by the
 * {@link com.sriky.popflix.adaptors.PopularMoviesFragmentPagerAdaptor} which is used by the
 * {@link android.support.v4.view.ViewPager} in the {@link PopularMoviesActivity} layout.
 */

public class PopularMoviesFragment extends Fragment
        implements PopularMoviesAdaptor.MoviePosterOnClickEventListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        FetchMovieDataTaskLoader.FetchMovieDataTaskListener {

    public static final String TAB_POSITION_BUNDLE_KEY = "tab_position";
    public static final String QUERY_PATH_BUNDLE_KEY = "query_path";

    private static final String TAG = PopularMoviesFragment.class.getSimpleName();

    /* identifiers that map fragments to Tab number */
    private static final int POPULAR_POSITION = 0;
    private static final int TOP_RATED_POSITION = 1;
    private static final int FAVORITE_POSITION = 2;

    /* the amount time to wait prior to displaying an error message if data isn't loader by the
     * time(millis) specified here.
     */
    private static final long DATA_LOAD_TIMEOUT_LIMIT = 30000;
    private static final long COUNT_DOWN_INTERVAL = 1000;

    private PopularMoviesBinding mPopularMoviesBinding;

    /* adaptor for the RecyclerView */
    private PopularMoviesAdaptor mPopularMoviesAdaptor;

    /* RecyclerView position */
    private int mPosition = RecyclerView.NO_POSITION;

    /* CountDownTimer used to issue a timeout when data doesn't load within the specified time. */
    private CountDownTimer mDataFetchTimer;

    /* position of the fragment that maps to the tab number */
    private int mFragmentPosition = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mPopularMoviesBinding = PopularMoviesBinding.inflate(inflater, container, false);

        showProgressBarAndHideErrorMessage();

        mPopularMoviesAdaptor = new PopularMoviesAdaptor(getContext(), this);

        mPopularMoviesBinding.rvPosters.setHasFixedSize(true);
        mPopularMoviesBinding.rvPosters.setAdapter(mPopularMoviesAdaptor);

        Bundle args = getArguments();
        if (args != null && args.containsKey(TAB_POSITION_BUNDLE_KEY)) {
            mFragmentPosition = args.getInt(TAB_POSITION_BUNDLE_KEY);
        } else if (savedInstanceState.containsKey(TAB_POSITION_BUNDLE_KEY)) {
            mFragmentPosition = savedInstanceState.getInt(TAB_POSITION_BUNDLE_KEY);
        }

        Log.d(TAG, "onCreateView() mFragmentPosition : " + mFragmentPosition);
        int loader_id = MovieDataUtils.BASIC_MOVIE_DATA_LOADER_ID;
        String queryPath;
        switch (mFragmentPosition) {
            case POPULAR_POSITION: {
                queryPath = getString(R.string.sort_order_popular);
                break;
            }

            case TOP_RATED_POSITION: {
                queryPath = getString(R.string.sort_order_top_rated);
                break;
            }

            case FAVORITE_POSITION: {
                queryPath = null;
                loader_id = MovieDataUtils.FAVORITES_MOVIES_LOADER_ID;
                break;
            }

            default: {
                throw new RuntimeException("Unsupported fragment position : " + mFragmentPosition);
            }
        }

        if (mFragmentPosition != FAVORITE_POSITION) {
            /* initialize the sync service if it isn't running already */
            MovieDataSyncUtils.initialize(getContext(), queryPath);
            /* set the bundle with queryPath for the loader */
            if (args == null) {
                args = new Bundle();
            }
            args.putString(QUERY_PATH_BUNDLE_KEY, queryPath);
        }

        /* trigger the AsyncTaskLoader to download movie data.
         * The following call will initialize a new loader if one doesn't exist.
         * If an old loader exist and has loaded the data, then onLoadFinished() will be triggered.*/
        getLoaderManager().initLoader(loader_id, args, PopularMoviesFragment.this);

        return mPopularMoviesBinding.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TAB_POSITION_BUNDLE_KEY, mFragmentPosition);
        super.onSaveInstanceState(outState);
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
        Log.d(TAG + mFragmentPosition, "onCreateLoader: id = " + id);

        switch (id) {
            case MovieDataUtils.BASIC_MOVIE_DATA_LOADER_ID: {

                /* set the selection clause for the db query */
                String selection = null;
                if (args.containsKey(QUERY_PATH_BUNDLE_KEY)) {
                    String queryPath = args.getString(QUERY_PATH_BUNDLE_KEY);
                    selection = queryPath.equals(getString(R.string.sort_order_popular))
                            ? MoviesContract.MoviesEntry.POPULAR + " =? "
                            : MoviesContract.MoviesEntry.TOP_RATED + " =? ";
                }

                return new CursorLoader(getContext(),
                        MoviesContract.MoviesEntry.CONTENT_URI,
                        MovieDataUtils.MOVIE_DATA_PROJECTION,
                        selection,
                        new String[]{"1"},
                        null);
            }

            case MovieDataUtils.FAVORITES_MOVIES_LOADER_ID: {
                /* query for entries with MoviesContract.MoviesEntry.USER_FAVOURITE = true */
                return new CursorLoader(getContext(),
                        MoviesContract.MoviesEntry.CONTENT_URI,
                        MovieDataUtils.MOVIE_DATA_PROJECTION,
                        MoviesContract.MoviesEntry.USER_FAVOURITE + " =? ",
                        new String[]{"1"},
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
        Log.d(TAG + mFragmentPosition, "onLoadFinished: data.length() = " + data.getCount());

        mPopularMoviesAdaptor.swapCursor(data);

        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;

        mPopularMoviesBinding.rvPosters.smoothScrollToPosition(mPosition);

        if (data.getCount() > 0) {
            onDataLoadComplete();
        } else if (mFragmentPosition == FAVORITE_POSITION) {
            /* if it is the favorites fragment and there cursor is empty then there are no movies
             * that added to favorites yet!*/
            mPopularMoviesBinding.tvErrorMsg.setText(getString(R.string.add_movies_to_favorite));
            hideProgressBarAndShowErrorMessage();
        } else {
            /* if the timer was running then cancel it. */
            cancelDataFetchTimer();
            /* will there is no data set up a countdown to display an error message in case
             * the data doesn't load in the specified time.
             */
            mDataFetchTimer = new CountDownTimer(DATA_LOAD_TIMEOUT_LIMIT, COUNT_DOWN_INTERVAL) {

                public void onTick(long millisUntilFinished) {
                    Log.i(TAG + mFragmentPosition, "waiting on data "
                            + millisUntilFinished / COUNT_DOWN_INTERVAL
                            + "secs remaining for timeout:!");
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

    @Override
    public void onPreExecute() {
        Log.d(TAG + mFragmentPosition, "onPreExecute: ()");
        showProgressBarAndHideErrorMessage();
    }

    @Override
    public void onClickedMovieId(int movieId) {
        try {
            Intent intent = new Intent(getContext(), MovieDetailActivity.class);
            intent.putExtra(MovieDataUtils.MOVIE_ID_INTENT_EXTRA_KEY, Integer.toString(movieId));
            startActivity(intent);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * If there were issues downloading data from TMDB, then hide the progress bar view and
     * display an error message to the user.
     */
    private void onDataLoadFailed() {
        Log.d(TAG + mFragmentPosition, "onDataLoadFailed()");
        hideProgressBarAndShowErrorMessage();
    }

    /**
     * Displays the progress bar and hides the error message views.
     */
    private void showProgressBarAndHideErrorMessage() {
        mPopularMoviesBinding.pbPopularMovies.setVisibility(View.VISIBLE);
        mPopularMoviesBinding.tvErrorMsg.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the progress bar view and makes the the error message view VISIBLE.
     */
    private void hideProgressBarAndShowErrorMessage() {
        mPopularMoviesBinding.pbPopularMovies.setVisibility(View.INVISIBLE);
        mPopularMoviesBinding.tvErrorMsg.setVisibility(View.VISIBLE);
    }

    /**
     * On successfully downloading data from the API
     */
    private void onDataLoadComplete() {
        Log.d(TAG + mFragmentPosition, "onDataLoadComplete()");
        /* hide the progress bar & the error msg view. */
        mPopularMoviesBinding.pbPopularMovies.setVisibility(View.INVISIBLE);
        mPopularMoviesBinding.tvErrorMsg.setVisibility(View.INVISIBLE);
        /* if the timer was running then cancel it. */
        cancelDataFetchTimer();
    }

    /**
     * If a {@link CountDownTimer} already exist, then cancel it.
     */
    private void cancelDataFetchTimer() {
        if (mDataFetchTimer != null) {
            Log.d(TAG, "cancelDataFetchTimer()");
            mDataFetchTimer.cancel();
            mDataFetchTimer = null;
        }
    }
}
