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

package com.sriky.popflix.adaptors;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.sriky.popflix.MovieDetailActivity;
import com.sriky.popflix.MovieReviewsFragment;
import com.sriky.popflix.OverviewFragment;

/**
 * PagerView Adaptor for displaying movie overview and reviews.
 */

public class DetailsFragmentPagerAdaptor extends FragmentPagerAdapter {

    private static final String TAG = DetailsFragmentPagerAdaptor.class.getSimpleName();

    private static final int INDEX_MOVIE_OVERVIEW = 0;
    private static final int INDEX_MOVIE_REVIEWS = 1;
    private static final int NUM_PAGES = 2;

    private Cursor mCursor;
    private String mMovieId;

    public DetailsFragmentPagerAdaptor(FragmentManager fm, Cursor cursor, String movieId) {
        super(fm);
        mCursor = cursor;
        mMovieId = movieId;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem: position = " + position);
        switch (position) {
            case INDEX_MOVIE_OVERVIEW: {
                OverviewFragment fragment = new OverviewFragment();
                /* pass the movie overview via a bundle. */
                Bundle args = new Bundle();
                args.putString(OverviewFragment.ARG_OVERVIEW_KEY,
                        mCursor.getString(MovieDetailActivity.INDEX_MOVIE_OVERVIEW));
                fragment.setArguments(args);
                return fragment;
            }

            case INDEX_MOVIE_REVIEWS: {
                MovieReviewsFragment fragment = new MovieReviewsFragment();
                /* pass the movie id via a bundle. */
                Bundle args = new Bundle();
                args.putString(MovieReviewsFragment.ARG_MOVIE_ID_KEY, mMovieId);
                fragment.setArguments(args);
                return fragment;
            }

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
