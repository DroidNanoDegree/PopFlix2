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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Fragment that is part of {@link android.support.v4.view.ViewPager} responsible for displaying
 * the movie overview.
 */

public class OverviewFragment extends Fragment {
    private static final String TAG = OverviewFragment.class.getSimpleName();

    public static final String ARG_OVERVIEW_KEY = "overview";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.movie_overview, container, false);
        TextView tv = rootView.findViewById(R.id.tv_overview);
        Bundle args = getArguments();
        tv.setText(args.getString(ARG_OVERVIEW_KEY));
        return rootView;
    }
}
