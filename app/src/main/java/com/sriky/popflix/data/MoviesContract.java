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

package com.sriky.popflix.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines the table and column names for the movies database.
 */
public class MoviesContract {

    /*
     * Using the package as CONTENT_AUTHORITY
     */
    public static final String CONTENT_AUTHORITY = "com.sriky.popflix";

    /*
     * Base URI using the CONTENT AUTHORITY.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     * Support path for movie data using the BASE_CONTENT_URI
     */
    public static final String PATH_MOVIES = "movies";

    public static final class MoviesEntry implements BaseColumns {
        /*
         * The base content URI to query the movie database.
         */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        /*
         * Table to store the movies data.
         */
        public static final String TABLE_NAME = "movies";

        /*
         * Movie ID returned by the API.
         */
        public static final String MOVIE_ID = "movie_id";

        /*
         * The path segment to locate and download movie thumbnail from.
         */
        public static final String MOVIE_POSTER_PATH = "poster_path";

        /*
         * The overview of the movie.
         */
        public static final String MOVIE_OVERVIEW = "overview";

        /*
         * Average ratings for the movie ranging from 0 to 10.
         */
        public static final String MOVIE_VOTE_AVERAGE = "vote_average";

        /*
         * Total number of votes received for the movie.
         */
        public static final String MOVIE_VOTE_COUNT = "vote_count";

        /*
         * Title of the movie.
         */
        public static final String MOVIE_TITLE = "title";

        /*
         * The date the movie was released.
         */
        public static final String MOVIE_RELEASE_DATE = "release_date";

        /*
         * Flag to indicated movies favourited by users.
         */
        public static final String USER_FAVOURITE = "user_favourite";

        /**
         * Builds the Uri to query the movies database for a specific movie represented by the
         * movieId.
         *
         * @param movieId MovieID that was returned by the API and sorted in the "Movie_ID" column
         *                in the movies database.
         * @return Uri to query a specific movie's data.
         */
        public static Uri buildMovieUriWithId(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }
}
