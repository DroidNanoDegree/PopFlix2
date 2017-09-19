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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sriky.popflix.data.MoviesContract.MoviesEntry;

/**
 * The ContentProvider for all of PopFlix's data. It supports bulk_insert, query, delete operations.
 */

public class MoviesProvider extends ContentProvider {

    /* The identifier for the movies directory */
    private static final int CODE_MOVIES = 100;
    /* The identifier per movie which would be represented by the movieId */
    private static final int CODE_MOVIE_WITH_ID = 101;
    /* UriMatcher used by MoviesProvider. */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    /* Handle to the SQLLiteOpenHelper */
    private MoviesDbHelper mMoviesDbHelper;

    /**
     * Builds the UriMatcher that will match each URI to the CODE_MOVIES and
     * CODE_MOVIE_WITH_ID constants defined above.
     *
     * @return A UriMatcher that correctly matches the constants for CODE_MOVIES and CODE_MOVIE_WITH_ID
     */
    public static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /* This Uri should resolve to "content://com.sriky.popflix/movies" to get all directories */
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES, CODE_MOVIES);

        /* This Uri should resolve to "content://com.sriky.popflix/movies/2343455678" to get info
        * for a particular movie represented by the movieId */
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY,
                MoviesContract.PATH_MOVIES + "/#", CODE_MOVIE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        /* initialize the DB helper used by this class for to perform the CRUD operations on the
         * movies database. */
        mMoviesDbHelper = new MoviesDbHelper(getContext());
        return true;
    }

    /**
     * Bulk insert items into the movies database.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES: {
                int rowsInserted = 0;
                db.beginTransaction();
                try {
                    /* Parse the values and entry them one by one. */
                    for (ContentValues value : values) {
                        long id = db.insert(MoviesEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                /* Notify the data change so the cursor gets notified to update the UI. */
                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            }

            default:
                return super.bulkInsert(uri, values);
        }
    }

    /**
     * Query the movies database for multiple rows or a single row speficied by the parameters.
     *
     * @param uri      The URI to query
     * @param strings  The list of columns to put into the cursor. If null, all columns are
     *                 included.
     * @param s        A selection criteria to apply when filtering rows. If null, then all
     *                 rows are included.
     * @param strings1 You may include ?s in selection, which will be replaced by
     *                 the values from selectionArgs, in order that they appear in the
     *                 selection.
     * @param s1       How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query. In our implementation,
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings,
                        @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        SQLiteDatabase db = mMoviesDbHelper.getReadableDatabase();
        Cursor cursorToRet;

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES: {
                cursorToRet = db.query(MoviesEntry.TABLE_NAME,
                        strings,
                        s,
                        strings1,
                        null,
                        null,
                        s1);
                break;
            }

            case CODE_MOVIE_WITH_ID: {
                /* get the movieId which will be the last segment in the Uri. */
                String movieId = uri.getLastPathSegment();
                /* set the selectionArgs to contain the movieId */
                String[] selectionArgs = {movieId};
                cursorToRet = db.query(MoviesEntry.TABLE_NAME,
                        strings,
                        MoviesEntry.MOVIE_ID + " =? ",
                        selectionArgs,
                        null,
                        null,
                        s1);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        /* Notify the data change so the cursor gets notified to update the UI. */
        cursorToRet.setNotificationUri(getContext().getContentResolver(), uri);
        return cursorToRet;
    }

    /**
     * Returns the supported MIME types(i.e. items and directory) by movie database.
     *
     * @param uri The query URI
     * @return The supported MIME type(i.e. items and directory).
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES: {
                return "vnd.android.cursor.dir" + "/" + MoviesContract.CONTENT_AUTHORITY + "/" +
                        MoviesContract.PATH_MOVIES;
            }

            case CODE_MOVIE_WITH_ID: {
                return "vnd.android.cursor.item" + "/" + MoviesContract.CONTENT_AUTHORITY + "/" +
                        MoviesContract.PATH_MOVIES;
            }

            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
    }

    /**
     * Insert an item into the movies database.
     *
     * @param uri           The URI specifying the the location for the item to be inserted.
     * @param contentValues Set of column_name/value pairs to add to the database.
     *                      This must not be {@code null}.
     * @return Uri pointing to the newly inserted row of data.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        Uri uriToRet;

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE_WITH_ID: {
                long id = db.insert(MoviesEntry.TABLE_NAME,
                        null,
                        contentValues);
                if (id != -1) {
                    uriToRet = ContentUris.withAppendedId(MoviesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return uriToRet;
    }

    /**
     * Deletes row(s) of data at the specified URI based on the selection criteria specified.
     *
     * @param uri     The full URI to query.
     * @param s       An optional restriction to apply to rows when deleting.
     * @param strings Used in conjunction with the selection statement
     * @return The number of row(s) deleted.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        int numOfItemsDeleted;

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES: {
                /* The use case here will be delete all the rows and return the number of rows deleted.
                *  Passing s = null would deleted the entire table which is not intended use case.
                *  Setting s = 1 will delete all the rows and return the numOfRows deleted.
                */
                if (null == s) s = "1";
                numOfItemsDeleted = db.delete(MoviesEntry.TABLE_NAME,
                        s,
                        strings);
                break;
            }

            case CODE_MOVIE_WITH_ID: {
                String movieId = uri.getLastPathSegment();
                String[] selectionArgs = {movieId};
                numOfItemsDeleted = db.delete(MoviesEntry.TABLE_NAME,
                        MoviesEntry.MOVIE_ID + " =? ",
                        selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }

        /* Notify the data change so the cursor gets notified to update the UI. */
        if (numOfItemsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numOfItemsDeleted;
    }


    @Override
    public void shutdown() {
        /* Always close the DB helper */
        mMoviesDbHelper.close();
        super.shutdown();
    }

    /**
     * TODO: Implementation pending based on the need. Currently, there is no use case for this method.
     *
     * @param uri
     * @param contentValues
     * @param s
     * @param strings
     * @return
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
