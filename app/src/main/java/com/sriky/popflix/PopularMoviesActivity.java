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

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sriky.popflix.adaptors.PopularMoviesFragmentPagerAdaptor;
import com.sriky.popflix.databinding.ActivityPopularMoviesBinding;

/**
 * Main Activity that is launched from the Launcher.
 * Displays a TabLayout to toggle between Popular, Top Rated and Favorite movies.
 */
public class PopularMoviesActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener,
        ViewPager.OnPageChangeListener {

    private static final String TAG = PopularMoviesActivity.class.getSimpleName();

    private ActivityPopularMoviesBinding mActivityPopularMoviesBinding;

    private PopularMoviesFragmentPagerAdaptor mPopularMoviesFragmentPagerAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityPopularMoviesBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_popular_movies);

        mPopularMoviesFragmentPagerAdaptor =
                new PopularMoviesFragmentPagerAdaptor(getSupportFragmentManager());

        mActivityPopularMoviesBinding.vpPosters.setAdapter(mPopularMoviesFragmentPagerAdaptor);
        mActivityPopularMoviesBinding.vpPosters.setOffscreenPageLimit(3);

        /* add the tabbed layout to listen to page change notifications so that the ViewPager
         * can be updated accordingly
         */
        mActivityPopularMoviesBinding.vpPosters.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(
                        mActivityPopularMoviesBinding.tlMoviesFilter));

        /* update the ViewPager to the appropriate page */
        mActivityPopularMoviesBinding.tlMoviesFilter.addOnTabSelectedListener(this);

        /* update the page index when page changes so that ViewPager can resize according to the
         * size of the view.
         */
        mActivityPopularMoviesBinding.vpPosters.addOnPageChangeListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        Log.d(TAG, "onTabSelected: tab.position: " + position);
        mActivityPopularMoviesBinding.vpPosters.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: position: " + position);
        mActivityPopularMoviesBinding.vpPosters.reMeasureCurrentPage(
                mActivityPopularMoviesBinding.vpPosters.getCurrentItem());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
