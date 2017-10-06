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

package com.sriky.popflix.adaptors;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.sriky.popflix.R;
import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adaptor for the PopularMoviesActivity's grid RecyclerView.
 */

public class PopularMoviesAdaptor extends RecyclerView.Adapter<PopularMoviesAdaptor.ImageViewHolder> {

    private static final String TAG = PopularMoviesAdaptor.class.getSimpleName();

    private final Context mContext;
    /* cursor to the movies table */
    private Cursor mCursor;

    private MoviePosterOnClickEventListener PopularMoviesAdaptorOnClickListener;

    public PopularMoviesAdaptor(Context context, MoviePosterOnClickEventListener moviePosterOnClickEventListener) {
        mContext = context;
        PopularMoviesAdaptorOnClickListener = moviePosterOnClickEventListener;
    }

    /**
     * When there is new data, swap the cursor to the update the UI.
     *
     * @param cursor The new cursor.
     */
    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.popularmovies_list_item, parent, false);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        /* move the cursor to the correct position */
        mCursor.moveToPosition(position);
        /* get the information at the current cursor's position */
        String relativePath = mCursor.getString(MovieDataUtils.INDEX_POSTER_PATH);
        Uri uri = NetworkUtils.getURLForImageWithRelativePathAndSize(relativePath, MovieDataUtils.getQueryThumbnailWidthPath());
        holder.mMovieThumbNailView.setTag(mCursor.getInt(MovieDataUtils.INDEX_MOVIE_ID));
        Picasso.with(mContext)
                .load(uri)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(holder.mMovieThumbNailView);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    public interface MoviePosterOnClickEventListener {
        void onClickedMovieId(int movieId);
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //will display the image poster/thumbnail.
        @BindView(R.id.iv_movie_thumbnail)
        ImageView mMovieThumbNailView;

        ImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mMovieThumbNailView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick()");
            /* retrieve the movie id we set as the tag in onBindViewHolder */
            PopularMoviesAdaptorOnClickListener.onClickedMovieId(
                    (int) v.getTag());
        }
    }
}
