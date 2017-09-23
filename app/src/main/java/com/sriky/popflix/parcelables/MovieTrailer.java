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

package com.sriky.popflix.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable that presents the Movie Trailer object, used to display movie trailers in
 * {@link com.sriky.popflix.MovieDetailActivity}
 */

public class MovieTrailer implements Parcelable {
    /* the trailer ID */
    private String mId;
    /* key to the site */
    private String mKey;
    /* trailer name */
    private String mName;
    /* site to access the trailer */
    private String mSite;

    /**
     * Helper to generate MovieData object from savedInstanceState.
     *
     * @param in parcel from the savedInstanceState.
     */
    protected MovieTrailer(Parcel in) {
        mId = in.readString();
        mKey = in.readString();
        mName = in.readString();
        mSite = in.readString();
    }

    public MovieTrailer(String id, String key, String name, String site) {
        mId = id;
        mKey = key;
        mName = name;
        mSite = site;
    }

    public String getId() {
        return mId;
    }

    public String getKey() {
        return mKey;
    }

    public String getName() {
        return mName;
    }

    public String getSite() {
        return mSite;
    }

    public String toString() {
        return mId + "--" + mKey + "--" + mName + "--" + mSite;
    }

    public static final Creator<MovieTrailer> CREATOR = new Creator<MovieTrailer>() {
        @Override
        public MovieTrailer createFromParcel(Parcel in) {
            return new MovieTrailer(in);
        }

        @Override
        public MovieTrailer[] newArray(int size) {
            return new MovieTrailer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mKey);
        parcel.writeString(mName);
        parcel.writeString(mSite);
    }
}
