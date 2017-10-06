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
 * Class to hold the movie reviews data.
 */

public class MovieReview implements Parcelable {
    private String mId;
    private String mAuthor;
    private String mReviewContent;

    protected MovieReview(Parcel in) {
        mId = in.readString();
        mAuthor = in.readString();
        mReviewContent = in.readString();
    }

    public MovieReview(String id, String author, String content) {
        mId = id;
        mAuthor = author;
        mReviewContent = content;
    }

    public String getId() {
        return mId;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getReviewContent() {
        return mReviewContent;
    }

    public String toString() {
        return mId + "--" + mAuthor + "--" + mReviewContent;
    }

    public static final Creator<MovieReview> CREATOR = new Creator<MovieReview>() {
        @Override
        public MovieReview createFromParcel(Parcel in) {
            return new MovieReview(in);
        }

        @Override
        public MovieReview[] newArray(int size) {
            return new MovieReview[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mAuthor);
        parcel.writeString(mReviewContent);
    }
}
