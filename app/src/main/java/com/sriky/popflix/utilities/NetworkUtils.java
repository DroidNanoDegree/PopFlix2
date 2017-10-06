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
    /* TMDB API */
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie";
    private static final String PARAM_QUERY_API_KEP = "api_key";
    /* path to the videos */
    private static final String PATH_VIDEOS = "videos";
    /* path to reviews */
    private static final String PATH_REVIEWS = "reviews";

    /* youtube */
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com";
    private static final String YOUTUBE_VIDEOS_PATH = "watch";
    private static final String YOUTUBE_QUERY_PARAM_KEY = "v";

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
     * Builds URL for the specified movieId using TMDB base URL.
     *
     * @param movieId The movie ID.
     * @param apiKey  API key for TMDB.
     * @return URL to query TMBD to get movies in the order specified by movieId param.
     */
    public static URL buildURL(String movieId, String apiKey) {
        Uri uri = buildUri(apiKey, movieId);
        return buildUrl(uri);
    }

    /**
     * Builds the URL to query TMDB API for trailer videos.
     *
     * @param movieId The Movie ID
     * @param apiKey  API key for TMDB.
     * @return URL to query trailers for the specific movie ID.
     */
    public static URL buildVideosURL(String movieId, String apiKey) {
        Uri uri = buildUri(apiKey, movieId, PATH_VIDEOS);
        return buildUrl(uri);
    }

    /**
     * Builds the URL to query API for reviews.
     *
     * @param movieId The movie ID for which the reviews.
     * @param apiKey  API key for TMDB.
     * @return URL to query reviews for the specific movie ID.
     */
    public static URL buildReviewsURL(String movieId, String apiKey) {
        Uri uri = buildUri(apiKey, movieId, PATH_REVIEWS);
        return buildUrl(uri);
    }

    /**
     * Builds the youtube Uri for the supplied video key.
     *
     * @param videoKey The key to the video.
     * @return Uri to youtube for the video identified by the video key.
     */
    public static Uri buildYoutubeUri(String videoKey) {
        return Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendPath(YOUTUBE_VIDEOS_PATH)
                .appendQueryParameter(YOUTUBE_QUERY_PARAM_KEY, videoKey)
                .build();
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

    /**
     * Builds the Uri for the supplied path and query parameter.
     *
     * @param queryParam The query parameter
     * @param paths      Paths to append to the base uri.
     * @return Returns an Uri after appending the path and query param.
     */
    private static Uri buildUri(String queryParam, String... paths) {
        Uri.Builder uriBuilder = Uri.parse(TMDB_BASE_URL).buildUpon();
        for (String path : paths) {
            uriBuilder.appendPath(path);
        }
        uriBuilder.appendQueryParameter(PARAM_QUERY_API_KEP, queryParam);
        return uriBuilder.build();
    }

    /**
     * Builds URL from the supplied URI.
     *
     * @param uri The URI from which the URL will be built.
     * @return URL.
     */
    private static URL buildUrl(Uri uri) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
