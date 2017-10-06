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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import com.sriky.popflix.data.MoviesContract.MoviesEntry;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

/**
 * Class to test the database used with PopFlix. Following are the tests:
 * 1). Creation of Database and tables.
 * 2). Inserting single item into the movies table.
 * 3). Inserting items with the same movie ID should replace old entry as per the defined contract.
 * 4). _ID field part of BaseColumns autoincrement as per the defined contract.
 * 5. Database version update dropping the old table.
 * 6. Deleting all items.
 */

public class TestMoviesDatabase {
    private static final String TAG = TestMoviesDatabase.class.getSimpleName();
    private final Context mContext =
            InstrumentationRegistry.getTargetContext();

    @Before
    public void before() {
        /* delete all items from the movies table */
        TestUtilities.deleteAllItemsInMoviesTable(mContext);
    }

    /**
     * Method to test creation of databases and tables.
     */
    @Test
    public void testCreateDb() {
        /*
         * Using HashSet to test a database with multiple tables was created properly.
         */
        final HashSet<String> tableNameHashSet = new HashSet<>();

        /* add the names of tables */
        tableNameHashSet.add(MoviesEntry.TABLE_NAME);

        MoviesDbHelper moviesDbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = moviesDbHelper.getReadableDatabase();
        /* Verify DB is open */
        String databaseIsNotOpen = "The database should be open and isn't";
        assertEquals(databaseIsNotOpen,
                true,
                db.isOpen());

        /* This Cursor will contain the names of each table in our database */
        Cursor tableNameCursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null);

        /*
         * Check to verify if DB was created correctly.
         */
        String errorInCreatingDatabase =
                "Error: This means that the database has not been created correctly";
        assertTrue(errorInCreatingDatabase,
                tableNameCursor.moveToFirst());

        /*
         * Loop through the cursor and remove table names from the HashSet.
         */
        do {
            tableNameHashSet.remove(tableNameCursor.getString(0));
        } while (tableNameCursor.moveToNext());

        /* If this fails, it means that the database doesn't contain the expected table(s) */
        assertTrue("Error: Your database was created without the expected tables.",
                tableNameHashSet.isEmpty());

        tableNameCursor.close();
        moviesDbHelper.close();
    }

    /**
     * Method to test insertion of single row in the movies tables.
     */
    @Test
    public void testInsertSingleRecordIntoMoviesTable() {

        /* Obtain movies values from TestUtilities */
        ContentValues testMovieValues = TestUtilities.createMoviesContentValues();

        MoviesDbHelper moviesDbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = moviesDbHelper.getWritableDatabase();
        /* Insert ContentValues into database and get a row ID back */
        long moviesRowId = db.insert(
                MoviesEntry.TABLE_NAME,
                null,
                testMovieValues);

        /* If the insert fails, database.insert returns -1 */
        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Unable to insert into the database";
        assertNotSame(insertFailed,
                valueOfIdIfInsertFails,
                moviesRowId);

        /*
         * Query the database and receive a Cursor.
         */
        Cursor moviesCursor = db.query(
                MoviesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        /* Cursor.moveToFirst will return false if there are no records returned from your query */
        String emptyQueryError = "Error: No Records returned from movies query";
        assertTrue(emptyQueryError,
                moviesCursor.moveToFirst());

        /* Verify that the returned results match the expected results */
        String expectedMoviesDidntMatchActual =
                "Expected movies values didn't match actual values.";
        TestUtilities.validateCursorWithContentValues(expectedMoviesDidntMatchActual,
                moviesCursor,
                testMovieValues);

        /*
         * Since before every method annotated with the @Test annotation, the database is
         * deleted, we can assume in this method that there should only be one record in our
         * movie table because we inserted it. If there is more than one record, an issue has
         * occurred.
         */
        assertFalse("Error: More than one record returned from movies query",
                moviesCursor.moveToNext());

        /* close the db & cursor */
        moviesDbHelper.close();
        moviesCursor.close();
    }

    /**
     * Test to validate replacement of items when there is a collision with the Movie_ID.
     */
    @Test
    public void testDuplicateMovieIdInsertBehaviorShouldReplace() {

        /* we need to clear all the items prior this test */
        deletedAllItemsInMoviesTable();

        /* Obtain movie values from TestUtilities */
        ContentValues testMovieValues = TestUtilities.createMoviesContentValues();

        /* Insert the ContentValues into database */
        MoviesDbHelper moviesDbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = moviesDbHelper.getWritableDatabase();
        db.insert(
                MoviesEntry.TABLE_NAME,
                null,
                testMovieValues);

        /* Insert the same ContentValues into database, where the movie_id is the same. */
        db.insert(
                MoviesEntry.TABLE_NAME,
                null,
                testMovieValues);

        /* Query for the movie records */
        Cursor newMoviesIdCursor = db.query(
                MoviesEntry.TABLE_NAME,
                new String[]{MoviesEntry.MOVIE_ID},
                null,
                null,
                null,
                null,
                null);

        String recordWithNewIdNotFound =
                "New record did not overwrite the previous record for the same movie ID.";
        assertTrue(recordWithNewIdNotFound,
                newMoviesIdCursor.getCount() == 1);

        /* close the cursor and db. */
        newMoviesIdCursor.close();
        moviesDbHelper.close();
    }

    /**
     * Tests to ensure that inserts into your database results in automatically
     * incrementing row IDs.
     */
    @Test
    public void testIntegerAutoincrement() {

        /* First, let's ensure we have some values in our table initially */
        testInsertSingleRecordIntoMoviesTable();

        /* Obtain movie values from TestUtilities */
        ContentValues testMovieValues = TestUtilities.createMoviesContentValues();

        long originalMovieId = testMovieValues.getAsLong(MoviesEntry.MOVIE_ID);

        MoviesDbHelper moviesDbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = moviesDbHelper.getWritableDatabase();
        long firstRowId = db.insert(
                MoviesEntry.TABLE_NAME,
                null,
                testMovieValues);

        /* Delete the row we just inserted to see if the database will reuse the rowID */
        db.delete(
                MoviesEntry.TABLE_NAME,
                "_ID == " + firstRowId,
                null);

        /*
         * Now we need to change the movie_id associated with our test content values because the
         * database policy is to replace identical movie ids on conflict.
         */
        long nextMovieIdAfterOriginalMovieId = originalMovieId + 1;
        testMovieValues.put(MoviesEntry.MOVIE_ID, nextMovieIdAfterOriginalMovieId);

        /* Insert ContentValues into database and get another row ID back */
        long secondRowId = db.insert(
                MoviesEntry.TABLE_NAME,
                null,
                testMovieValues);

        String sequentialInsertsDoNotAutoIncrementId =
                "IDs were reused and shouldn't be if autoincrement is setup properly.";
        assertNotSame(sequentialInsertsDoNotAutoIncrementId,
                firstRowId, secondRowId);

        /* close the db. */
        db.close();
        moviesDbHelper.close();
    }

    /**
     * This method tests the {@link MoviesDbHelper#onUpgrade(SQLiteDatabase, int, int)}. The proper
     * behavior for this method in our case is to simply DROP (or delete) the movie table from
     * the database and then have the table recreated.
     */
    @Test
    public void testOnUpgradeBehavesCorrectly() {

        testInsertSingleRecordIntoMoviesTable();

        MoviesDbHelper moviesDBHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = moviesDBHelper.getWritableDatabase();
        moviesDBHelper.onUpgrade(db, 13, 14);

        /*
         * This Cursor will contain the names of each table in our database and we will use it to
         * make sure that our movie table is still in the database after upgrading.
         */
        Cursor tableNameCursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + MoviesEntry.TABLE_NAME + "'",
                null);

        /*
         * Our database should only contain one table, and so the above query should have one
         * record in the cursor that queried for our table names.
         */
        int expectedTableCount = 1;
        String shouldHaveSingleTable = "There should only be one table returned from this query.";
        assertEquals(shouldHaveSingleTable,
                expectedTableCount,
                tableNameCursor.getCount());

        /* We are done verifying our table names, so we can close this cursor */
        tableNameCursor.close();

        Cursor shouldBeEmptyMoviesCursor = db.query(
                MoviesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        int expectedRecordCountAfterUpgrade = 0;
        /* We will finally verify that our movie table is empty after */
        String movieTableShouldBeEmpty =
                "movie table should be empty after upgrade, but wasn't."
                        + "\nNumber of records: ";
        assertEquals(movieTableShouldBeEmpty,
                expectedRecordCountAfterUpgrade,
                shouldBeEmptyMoviesCursor.getCount());

        /* close the db. */
        db.close();
        shouldBeEmptyMoviesCursor.close();
        moviesDBHelper.close();
    }

    /**
     * Deletes all the items in the movies table.
     */
    @Test
    public void deletedAllItemsInMoviesTable() {
        MoviesDbHelper moviesDBHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = moviesDBHelper.getWritableDatabase();

        /* query to get a cursor so can find out the number of items
        * in the movies tables prior to deleting
        * */
        Cursor cursor = db.query(MoviesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        int numOfRowPriorToDeleting = cursor.getCount();
        int rowsDeleted = db.delete(MoviesEntry.TABLE_NAME,
                null,
                null);

        String unableToDeleteAllItems = "Unable to delete all items from the movies table.";
        assertEquals(unableToDeleteAllItems,
                numOfRowPriorToDeleting,
                rowsDeleted);

        /* close the cursor and db. */
        cursor.close();
        db.close();
        moviesDBHelper.close();
    }
}
