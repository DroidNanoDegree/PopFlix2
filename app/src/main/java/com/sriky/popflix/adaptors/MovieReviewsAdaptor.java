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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sriky.popflix.R;
import com.sriky.popflix.parcelables.MovieReview;

import java.util.ArrayList;

/**
 * The adaptor the reviews {@link RecyclerView}
 */

public class MovieReviewsAdaptor
        extends RecyclerView.Adapter<MovieReviewsAdaptor.ReviewsViewHolder> {

    private ArrayList<MovieReview> mMovieReviewList;
    private Context mContext;

    public MovieReviewsAdaptor(@NonNull Context context) {
        mContext = context;
    }

    public void updateDataSource(ArrayList<MovieReview> movieReviews) {
        mMovieReviewList = movieReviews;
        notifyDataSetChanged();
    }

    @Override
    public ReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.reviews_list_item, parent, false);
        return new ReviewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewsViewHolder holder, int position) {
        MovieReview movieReview = mMovieReviewList.get(position);
        holder.author.setText(movieReview.getAuthor());
        holder.reviewContent.setText(movieReview.getReviewContent());
    }

    @Override
    public int getItemCount() {
        if (mMovieReviewList == null) return 0;

        return mMovieReviewList.size();
    }

    public class ReviewsViewHolder extends RecyclerView.ViewHolder {
        public TextView author;
        public TextView reviewContent;

        public ReviewsViewHolder(View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.tv_author);
            reviewContent = itemView.findViewById(R.id.tv_reviewContent);
        }
    }
}
