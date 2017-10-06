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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sriky.popflix.R;
import com.sriky.popflix.parcelables.MovieTrailer;

import java.util.ArrayList;

/**
 * Implements the adaptor for the ListView used to display movie trailers
 */

public class MovieTrailerAdaptor extends RecyclerView.Adapter<MovieTrailerAdaptor.TrailerImageViewHolder> {

    public interface MovieTrailerOnClickedListener {
        void onClicked(String trailerKey);
    }

    private static final String TAG = MovieTrailerAdaptor.class.getSimpleName();
    private ArrayList<MovieTrailer> mMovieTrailersList;
    private Context mContext;
    private MovieTrailerOnClickedListener mMovieTrailerOnClickedListener;


    public MovieTrailerAdaptor(@NonNull Context context,
                               MovieTrailerOnClickedListener movieTrailerOnClickedListener) {
        mMovieTrailerOnClickedListener = movieTrailerOnClickedListener;
        mContext = context;
    }

    /**
     * Updates the recycler view with the new items from the ArrayList.
     *
     * @param trailers ArrayList containing the MovieTrailer objects.
     */
    public void updateTrailers(ArrayList<MovieTrailer> trailers) {
        mMovieTrailersList = trailers;
        notifyDataSetChanged();
    }

    @Override
    public TrailerImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder()");
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.trailers_list_item, parent, false);

        return new TrailerImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerImageViewHolder holder, int position) {
        MovieTrailer movieTrailer = mMovieTrailersList.get(position);
        holder.trailerButton.setText(movieTrailer.getName());
        holder.trailerButton.setTag(movieTrailer.getKey());
    }

    @Override
    public int getItemCount() {
        if (mMovieTrailersList != null) {
            Log.d(TAG, "getCount: " + mMovieTrailersList.size());
            return mMovieTrailersList.size();
        }
        return 0;
    }

    public class TrailerImageViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public Button trailerButton;

        public TrailerImageViewHolder(View itemView) {
            super(itemView);
            trailerButton = itemView.findViewById(R.id.btn_trailer);
            trailerButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mMovieTrailerOnClickedListener.onClicked((String) trailerButton.getTag());
        }
    }
}
