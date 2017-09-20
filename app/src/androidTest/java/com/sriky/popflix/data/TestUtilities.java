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

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.sriky.popflix.data.MoviesContract.MoviesEntry;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Class contains utility methods to support the Test cases.
 */

public class TestUtilities {
    /**
     * Creates and returns ContentValues that represents an item in the "movies" table.
     *
     * @return ContentValue.
     */
    public static ContentValues createMoviesContentValues() {
        /* generate a random movie ID */
        final int min = 20;
        final int max = 80;
        final Random random = new Random();
        int movieId = random.nextInt((max - min) + 1) + min;

        ContentValues cv = new ContentValues();
        cv.put(MoviesEntry.MOVIE_ID, movieId);
        cv.put(MoviesEntry.MOVIE_TITLE, "Test " + movieId);
        cv.put(MoviesEntry.MOVIE_OVERVIEW, "This movie is about testing the movies database!");
        cv.put(MoviesEntry.MOVIE_POSTER_PATH, "www.google.com");
        cv.put(MoviesEntry.MOVIE_RELEASE_DATE, "2017-09-18");
        cv.put(MoviesEntry.MOVIE_VOTE_AVERAGE, "9.5");
        cv.put(MoviesEntry.MOVIE_VOTE_COUNT, "656");
        return cv;
    }

    /**
     * Creates and returns ContentValues Array where each element represents an item in the "movies" table.
     *
     * @return ContentValue[]
     */
    public static ContentValues[] createMoviesContentValuesArray() {
        ContentValues[] cvArray = new ContentValues[3];
        cvArray[0] = createMoviesContentValues();
        cvArray[1] = createMoviesContentValues();
        cvArray[2] = createMoviesContentValues();
        return cvArray;
    }

    /**
     * Validates the supplied cursor with contentValues and asserts if they don't match or
     * cursor is null or empty!
     *
     * @param error          The error message to display.
     * @param cursor         The cursor returned after a query.
     * @param contentValues  The contentValues to compare the cursor items with.
     */
    public static void validateCursorWithContentValues(String error,
                                                       Cursor cursor, ContentValues contentValues) {

        /* sanity checks for valid cursor */
        assertNotNull("This cursor is null!!!", cursor);
        if(cursor.getPosition() == -1) {
            assertTrue("Empty cursor returned! " + error, cursor.moveToFirst());
        }

        Set<Map.Entry<String, Object>> valueSet = contentValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int index = cursor.getColumnIndex(columnName);

            /* Test to see if the column is contained within the cursor */
            String columnNotFoundError = "Column '" + columnName + "' not found. "
                    + error;
            assertFalse(columnNotFoundError, index == -1);

            /* Test to see if the expected value equals the actual value (from the Cursor) */
            String expectedValue = entry.getValue().toString();
            String actualValue = cursor.getString(index);

            String valuesDontMatchError = "Actual value '" + actualValue
                    + "' did not match the expected value '" + expectedValue + "'. "
                    + error;

            assertEquals(valuesDontMatchError,
                    expectedValue,
                    actualValue);
        }
    }

    /**
     * Helper method to delete all entries in the movies table.
     */
    public static void deleteAllItemsInMoviesTable(Context context) {
        MoviesDbHelper moviesDBHelper = new MoviesDbHelper(context);
        SQLiteDatabase db = moviesDBHelper.getWritableDatabase();

        db.delete(MoviesEntry.TABLE_NAME,
                null,
                null);

    }
}
