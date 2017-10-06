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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.sriky.popflix.PopularMoviesFragment;

/**
 * FragmentAdaptor for the {@link android.support.v4.view.ViewPager} used in activity_popular_movies.
 */

public class PopularMoviesFragmentPagerAdaptor extends FragmentPagerAdapter {
    private static final String TAG = PopularMoviesFragmentPagerAdaptor.class.getSimpleName();
    private static final int NUM_PAGES = 3;

    public PopularMoviesFragmentPagerAdaptor(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem() position = " + position);
        PopularMoviesFragment popularMoviesFragment = new PopularMoviesFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PopularMoviesFragment.TAB_POSITION_BUNDLE_KEY, position);
        popularMoviesFragment.setArguments(bundle);
        return popularMoviesFragment;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
