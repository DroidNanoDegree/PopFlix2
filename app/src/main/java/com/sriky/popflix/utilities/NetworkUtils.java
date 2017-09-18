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

package com.sriky.popflix.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Helper class(static) to assist in generating URLs and performing API requests.
 */
public final class NetworkUtils {
    private static final String TMDA_BASE_URL = "https://api.themoviedb.org/3/movie";
    private static final String PARAM_QUERY_API_KEP = "api_key";

    //images
    private static final String TMDA_IMAGE_BASE_URL = "http://image.tmdb.org/t/p";

    /**
     * Builds and returns Uri from the supplied relative path.
     *
     * @param encodedRelativePath - encoded relative path location at TMDB.
     * @param imageWidthPath      - path that specifies the thumbnail width.
     * @return complete query Uri to TMDB.
     */
    public static Uri getURLForImageWithRelativePathAndSize(String encodedRelativePath, String imageWidthPath) {
        return Uri.parse(TMDA_IMAGE_BASE_URL).buildUpon()
                .appendPath(imageWidthPath)
                .appendEncodedPath(encodedRelativePath)
                .build();
    }

    /**
     * Builds URL for the specified path using TMDB base URL.
     *
     * @param path   - query parameter for desired ordering of the movie.
     * @param apiKey - API key for TMDB.
     * @return URL to query TMBD to get movies in the order specified by path param.
     */
    public static URL buildURL(String path, String apiKey) {
        Uri uri = Uri.parse(TMDA_BASE_URL).buildUpon()
                .appendPath(path)
                .appendQueryParameter(PARAM_QUERY_API_KEP, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The String contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getStringResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
