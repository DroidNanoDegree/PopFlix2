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

package com.sriky.popflix;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.sriky.popflix.utilities.MovieDataUtils;
import com.sriky.popflix.utilities.NetworkUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adaptor for the PopularMoviesActivity's grid RecyclerView.
 */

class PopularMoviesAdaptor extends RecyclerView.Adapter<PopularMoviesAdaptor.ImageViewHolder> {

    private static final String TAG = PopularMoviesAdaptor.class.getSimpleName();

    //total number of movie posters that will in the grid layout.
    private int mNumberOfItems;

    private MoviePosterOnClickEventListener PopularMoviesAdaptorOnClickListener;

    PopularMoviesAdaptor(int numberOfItems, MoviePosterOnClickEventListener moviePosterOnClickEventListener) {
        Log.d(TAG, "PopularMoviesAdaptor: numberOfItems = " + numberOfItems);
        mNumberOfItems = numberOfItems;
        PopularMoviesAdaptorOnClickListener = moviePosterOnClickEventListener;
    }

    public void updateItemsCount(int newItemsCount){
        mNumberOfItems = newItemsCount;
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
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mNumberOfItems;
    }

    interface MoviePosterOnClickEventListener {
        void onClickedItemAt(int index);
    }

    class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //will display the image poster/thumbnail.
        @BindView(R.id.iv_movie_thumbnail)
        ImageView mMovieThumbNailView;

        ImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mMovieThumbNailView.setOnClickListener(this);
        }

        /**
         * Method to set the poster image from the list of posters.
         *
         * @param listIndex Position of the item in the list
         */
        void bind(int listIndex) {
            PopularMoviesActivity popularMoviesActivity = (PopularMoviesActivity) mMovieThumbNailView.getContext();
            String relativePath = popularMoviesActivity.getImageRelativePathAtIndex(listIndex);
            Uri uri = NetworkUtils.getURLForImageWithRelativePathAndSize(relativePath, MovieDataUtils.getQueryThumbnailWidthPath());
            Picasso.with(popularMoviesActivity)
                    .load(uri)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(mMovieThumbNailView);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick()");
            PopularMoviesAdaptorOnClickListener.onClickedItemAt(getAdapterPosition());
        }
    }
}
