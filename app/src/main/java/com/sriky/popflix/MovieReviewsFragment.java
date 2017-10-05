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


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sriky.popflix.adaptors.MovieReviewsAdaptor;
import com.sriky.popflix.databinding.MovieReviewsBinding;
import com.sriky.popflix.loaders.FetchMovieDataTaskLoader;
import com.sriky.popflix.parcelables.MovieReview;
import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;

/**
 * Fragment that is part of {@link android.support.v4.view.ViewPager} responsible for displaying
 * the user reviews for movies.
 */

public class MovieReviewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = MovieReviewsFragment.class.getSimpleName();

    public static final String ARG_MOVIE_ID_KEY = "movie_id";

    private ArrayList<MovieReview> mMovieReviewList;
    private MovieReviewsAdaptor mMovieReviewsAdaptor;

    private MovieReviewsBinding mMovieReviewsBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        mMovieReviewsBinding = MovieReviewsBinding.inflate(inflater, container, false);

        showProgressBarAndHideErrorMessage();

        /* set up the RecyclerView */
        mMovieReviewsBinding.reviewsRecyclerView.setHasFixedSize(true);
        mMovieReviewsBinding.reviewsRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mMovieReviewsAdaptor = new MovieReviewsAdaptor(getContext());
        mMovieReviewsBinding.reviewsRecyclerView.setAdapter(mMovieReviewsAdaptor);

        /* get the movie ID from the bundle */
        Bundle args = getArguments();
        String movieId;
        if (args.containsKey(ARG_MOVIE_ID_KEY)) {
            movieId = args.getString(ARG_MOVIE_ID_KEY);
        } else {
            throw new RuntimeException("MovieId not set!");
        }

        /* build the url for query reviews for a specific movie ID */
        URL url = NetworkUtils.buildReviewsURL(movieId, MovieDataUtils.TMDB_API_KEY);
        Bundle loaderBundle = new Bundle();
        loaderBundle.putString(MovieDataUtils.FETCH_MOVIE_DATA_URL_KEY, url.toString());

        /* trigger the FetchMovieDataTaskLoader to download reviews.
         * The following call will initialize a new loader if one doesn't exist.
         * If an old loader exist and has loaded the data, then onLoadFinished() will be triggered. */
        getLoaderManager().initLoader(MovieDataUtils.REVIEWS_DATA_LOADER_ID,
                loaderBundle, MovieReviewsFragment.this);

        return mMovieReviewsBinding.getRoot();
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new FetchMovieDataTaskLoader(getContext(), args);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null) {
            Log.d(TAG, "onLoadFinished() data : " + data);
            mMovieReviewList = MovieDataUtils.getMovieReviews(data);
            if (mMovieReviewList.size() == 0) {
                mMovieReviewsBinding.tvErrorMsg.setText(getString(R.string.no_reviews));
                hideProgressBarAndShowErrorMessage();
            } else {
                mMovieReviewsBinding.pbPopularMovies.setVisibility(View.INVISIBLE);
                mMovieReviewsAdaptor.updateDataSource(mMovieReviewList);
            }
        } else {
            hideProgressBarAndShowErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    /**
     * Displays the progress bar and hides the error message views.
     */
    private void showProgressBarAndHideErrorMessage() {
        mMovieReviewsBinding.pbPopularMovies.setVisibility(View.VISIBLE);
        mMovieReviewsBinding.tvErrorMsg.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the progress bar view and makes the the error message view VISIBLE.
     */
    private void hideProgressBarAndShowErrorMessage() {
        mMovieReviewsBinding.pbPopularMovies.setVisibility(View.INVISIBLE);
        mMovieReviewsBinding.tvErrorMsg.setVisibility(View.VISIBLE);
    }
}
