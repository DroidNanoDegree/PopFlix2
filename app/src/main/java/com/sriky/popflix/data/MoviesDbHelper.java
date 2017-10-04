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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sriky.popflix.data.MoviesContract.MoviesEntry;
/**
 * Manages the local database for movies.
 */

public class MoviesDbHelper extends SQLiteOpenHelper {

    /*
     * Database name to store the movies data.
     */
    public static final String DATABASE_NAME = "movies.db";

    /*
     * Database version, to be utilized when database scheme changes(i.e adding or removing db columns).
     */
    public static final int DATABASE_VERSION = 2;

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the movies database to store data returned by the API.
     *
     * @param sqLiteDatabase The movies database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                MoviesEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesEntry.MOVIE_ID           + " INTEGER NOT NULL, " +
                MoviesEntry.MOVIE_TITLE        + " TEXT NOT NULL, " +
                MoviesEntry.MOVIE_POSTER_PATH  + " TEXT NOT NULL, " +
                MoviesEntry.MOVIE_OVERVIEW     + " MEDIUMTEXT NOT NULL, " +
                MoviesEntry.MOVIE_RELEASE_DATE + " INTEGER NOT NULL, " +
                MoviesEntry.MOVIE_VOTE_AVERAGE + " INTEGER NOT NULL, " +
                MoviesEntry.MOVIE_VOTE_COUNT   + " INTEGER NOT NULL, " +
                MoviesEntry.USER_FAVOURITE     + " BIT, " +
                MoviesEntry.POPULAR            + " BIT, " +
                MoviesEntry.TOP_RATED          + " BIT, " +
                /* ensuring the table will contain single entry per MovieId.
                 * If there is a conflict the old entry will be replaced with a new entry
                 */
                " UNIQUE (" + MoviesEntry.MOVIE_ID + ") ON CONFLICT REPLACE);";

        //create the table from the creation statement string defined above.
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
