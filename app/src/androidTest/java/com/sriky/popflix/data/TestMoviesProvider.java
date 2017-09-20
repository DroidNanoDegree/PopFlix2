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

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import com.sriky.popflix.data.MoviesContract.MoviesEntry;

/**
 * This class tests various database operations that are supported by ContentProvider(i.e. MoviesProvider).
 * The tests validates accurate working of the following to be:
 * <p>
 * 1). UriMatcher
 * 2). Provider entry in the AndroidManifest.
 * </p>
 */

public class TestMoviesProvider {
    /* Context used to access various parts of the system */
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    @Before
    public void init() {
        TestUtilities.deleteAllItemsInMoviesTable(mContext);
    }
    /**
     * This function tests the UriMatcher returns the correct integer value for
     * each of the Uri types that MoviesProvider can handle.
     */
    @Test
    public void testUriMatcher() {

        /* Test that the code returned from our matcher matches the expected movies directory code */
        String movieUriDoesNotMatch = "Error: The CODE_MOVIES URI was matched incorrectly!";
        UriMatcher uriMatcher = MoviesProvider.buildUriMatcher();
        int actualMovieCode = uriMatcher.match(MoviesContract.MoviesEntry.CONTENT_URI);
        int expectedMovieCode = MoviesProvider.CODE_MOVIES;
        assertEquals(movieUriDoesNotMatch,
                expectedMovieCode,
                actualMovieCode);

        /*
         * Test that the code returned from our matcher matches the expected movie item with movie ID.
         */
        String movieWithMovieIdUriCodeDoesNotMatch =
                "Error: The CODE_MOVIE_WITH_ID WITH movie ID URI was matched incorrectly!";
        String randomMovieId = "1234554564333";
        int actualMovieWithMovieIdCode = uriMatcher.match(
                MoviesContract.MoviesEntry.CONTENT_URI.buildUpon().appendPath(randomMovieId).build());
        int expectedMovieWithMovieIdCode = MoviesProvider.CODE_MOVIE_WITH_ID;
        assertEquals(movieWithMovieIdUriCodeDoesNotMatch,
                expectedMovieWithMovieIdCode,
                actualMovieWithMovieIdCode);
    }

    /**
     * This test checks to make sure that the content provider is registered correctly in the
     * AndroidManifest file. If it fails, you should check the AndroidManifest to see if you've
     * added a <provider/> tag and that you've properly specified the android:authorities attribute.
     * <p>
     * Potential causes for failure:
     * <p>
     *   1) Your MoviesProvider was registered with the incorrect authority
     * <p>
     *   2) Your MoviesProvider was not registered at all
     */
    @Test
    public void testProviderRegistry() {
        /*
         * We will use the ComponentName for our ContentProvider class to ask the system
         * information about the ContentProvider, specifically, the authority under which it is
         * registered.
         */
        String packageName = mContext.getPackageName();
        String moviesProviderClassName = MoviesProvider.class.getName();
        ComponentName componentName = new ComponentName(packageName, moviesProviderClassName);

        try {
            PackageManager pm = mContext.getPackageManager();

            /* The ProviderInfo will contain the authority, which is what we want to test */
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = MoviesContract.CONTENT_AUTHORITY;

            /* Make sure that the registered authority matches the authority from the Contract */
            String incorrectAuthority =
                    "Error: MoviesProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthority,
                    actualAuthority,
                    expectedAuthority);

        } catch (PackageManager.NameNotFoundException e) {
            String providerNotRegisteredAtAll =
                    "Error: MoviesProvider not registered at " + mContext.getPackageName();
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll);
        }
    }

    /**
     * Tests basic query of movies table via the content provider. For this test we access the
     * database directly to insert the data and query it using the content provider.
     * {@link MoviesProvider#insert(Uri, ContentValues)} is tested in a separate test case.
     */
    @Test
    public void testBasicMoviesQuery() {
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        /* Obtain movie values from TestUtilities */
        ContentValues testMovieValues = TestUtilities.createMoviesContentValues();

        /* Insert ContentValues into database and get a row ID back */
        long moviesRowId = database.insert(
                MoviesContract.MoviesEntry.TABLE_NAME,
                null,
                testMovieValues);

        String insertFailed = "Unable to insert into the database";
        assertTrue(insertFailed, moviesRowId != -1);

        /* Query the movies table */
        Cursor moviesCursor = mContext.getContentResolver().query(
                MoviesContract.MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        /* Validate the cursor with the contentValues we used to make the entry. */
        TestUtilities.validateCursorWithContentValues("testBasicMoviesQuery",
                moviesCursor,
                testMovieValues);

        /* close the db & cursor. */
        database.close();
        dbHelper.close();
        moviesCursor.close();
    }

    /**
     * This test tests the bulkInsert feature of the ContentProvider.
     */
    @Test
    public void testBulkInsert() {

        /* Create a new array of ContentValues for movies */
        ContentValues[] bulkInsertTestContentValues = TestUtilities.createMoviesContentValuesArray();

        /* Using ContentResolver to access to the content model to perform the queries */
        ContentResolver contentResolver = mContext.getContentResolver();

        /* bulkInsert will return the number of records that were inserted. */
        int insertCount = contentResolver.bulkInsert(
                MoviesEntry.CONTENT_URI,
                bulkInsertTestContentValues);

        /*
         * Verify the value returned by the ContentProvider after bulk insert with the number of
         * item in the bulkInsertTestContentValues. They should match! */
        String expectedAndActualInsertedRecordCountDoNotMatch =
                "Number of expected records inserted does not match actual inserted record count";
        assertEquals(expectedAndActualInsertedRecordCountDoNotMatch,
                insertCount,
                bulkInsertTestContentValues.length);

        /* Query the movies table and verify if all the entries match with
         * bulkInsertTestContentValues array */
        Cursor cursor = mContext.getContentResolver().query(
                MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        /* For sanity, we can verify the items in the cursor as well. */
        assertEquals(cursor.getCount(), bulkInsertTestContentValues.length);

        /*
         * We now loop through and validate each record in the Cursor with the expected values from
         * bulkInsertTestContentValues.
         */
        for (int i = 0; i < bulkInsertTestContentValues.length; i++, cursor.moveToNext()) {
            TestUtilities.validateCursorWithContentValues(
                    "testBulkInsert. Error validating MoviesEntry " + i,
                    cursor,
                    bulkInsertTestContentValues[i]);
        }
        cursor.close();
    }

    /**
     * This test deletes all records from the movies table using the ContentProvider.
     */
    @Test
    public void testDeleteAllRecordsFromProvider() {

        /* Ensure there are records to delete from the database. */
        testBulkInsert();

        /* Using ContentResolver to access to the content model to perform the queries */
        ContentResolver contentResolver = mContext.getContentResolver();

        /* Delete all of the rows of data from the movies table */
        contentResolver.delete(
                MoviesEntry.CONTENT_URI,
                null,
                null);

        /* Perform a query of the data that we've just deleted and the cursor count should be 0. */
        Cursor shouldBeEmptyCursor = contentResolver.query(
                MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        /* assert if the returned cursor is null. */
        String cursorWasNull = "Cursor was null.";
        assertNotNull(cursorWasNull, shouldBeEmptyCursor);

        /* check for cursor count = 0. */
        String allRecordsWereNotDeleted =
                "Error: All records were not deleted from movies table during delete";
        assertEquals(allRecordsWereNotDeleted,
                0,
                shouldBeEmptyCursor.getCount());

        /* Always close your cursor */
        shouldBeEmptyCursor.close();
    }
}
