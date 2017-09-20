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

package com.sriky.popflix.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

/**
 * {@link AsyncTaskLoader} to fetch movie data from the supplied URL and returns the string results.
 * In-order to receive callbacks for onPreExecute the calling class needs to implement either
 * FetchMovieDataTaskListener.
 */

public class FetchMovieDataTaskLoader extends AsyncTaskLoader<String> {

    /**
     * Base Interface for routing async task callbacks.
     */
    public interface FetchMovieDataTaskListener {
        void onPreExecute();
    }

    private static final String TAG = FetchMovieDataTaskLoader.class.getSimpleName();

    /* Handle to the listener */
    private FetchMovieDataTaskListener mFetchMovieDataTaskListener;

    /* Bundle containing query URL */
    private Bundle mLoaderArgs;

    /* Cache and hold the result to prevent additional load calls during activity
       backgrouded etc. */
    private String mResult;

    public FetchMovieDataTaskLoader(Context context, Bundle loaderArgs) {
        super(context);
        mLoaderArgs = loaderArgs;
        if (context instanceof FetchMovieDataTaskListener) {
            mFetchMovieDataTaskListener = (FetchMovieDataTaskListener) context;
        }
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading()");
        if (mResult != null) {
            deliverResult(mResult);
        } else {
            if (mFetchMovieDataTaskListener != null) {
                mFetchMovieDataTaskListener.onPreExecute();
            }
            forceLoad();
        }
    }

    @Override
    public String loadInBackground() {
        String result = null;
        if (mLoaderArgs != null && mLoaderArgs.containsKey(MovieDataUtils.FETCH_MOVIE_DATA_URL_KEY)) {
            String urlStr = mLoaderArgs.getString(MovieDataUtils.FETCH_MOVIE_DATA_URL_KEY);
            Log.d(TAG, "loadInBackground: URL = " + urlStr);
            try {
                URL url = new URL(urlStr);
                result = NetworkUtils.getStringResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void deliverResult(String data) {
        Log.d(TAG, "deliverResult()");
        mResult = data;
        super.deliverResult(data);
    }
}
