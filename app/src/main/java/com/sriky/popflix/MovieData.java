package com.sriky.popflix;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents movie information gathered from TMDB API.
 */

public class MovieData implements Parcelable {
    //part of the url path for thumbnail poster
    private String mPosterPath;

    //overview or plot synopsis of the movie
    private String mOverview;

    //average vote for the movie.
    private String mVoteAverage;

    //movie id.
    private String mMovieID;

    //movie title
    private String mTitle;

    //movie release date
    private String mReleaseDate;

    public MovieData() {
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public void setVoteAverage(String voteAverage) {
        mVoteAverage = voteAverage;
    }

    public void setMovieID(String movieID) {
        mMovieID = movieID;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public String getOverview() {
        return mOverview;
    }

    public String getVoteAverage() {
        return mVoteAverage;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getMovieID() {
        return mMovieID;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String toString() {
        return mPosterPath + "--" + mMovieID + "--" + mOverview + "--"
                + mVoteAverage + "--" + mTitle + "--" + mReleaseDate;
    }

    /**
     * Helper to generate MovieData object from savedInstanceState.
     *
     * @param in parcel from the savedInstanceState.
     */
    protected MovieData(Parcel in) {
        mPosterPath = in.readString();
        mMovieID = in.readString();
        mOverview = in.readString();
        mVoteAverage = in.readString();
        mTitle = in.readString();
        mReleaseDate = in.readString();
    }

    public static final Creator<MovieData> CREATOR = new Creator<MovieData>() {
        @Override
        public MovieData createFromParcel(Parcel in) {
            return new MovieData(in);
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPosterPath);
        dest.writeString(mMovieID);
        dest.writeString(mOverview);
        dest.writeString(mVoteAverage);
        dest.writeString(mTitle);
        dest.writeString(mReleaseDate);
    }
}
