package com.devtau.popularmoviess2.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.model.Review;
import java.util.ArrayList;
/**
 * Адаптер списка рецензий
 * Adapter for list of reviews
 */
public class ReviewsRecyclerViewAdapter extends RecyclerView.Adapter<ReviewsRecyclerViewAdapter.ReviewsViewHolder>{
    private ArrayList<Review> reviewsList;

    public ReviewsRecyclerViewAdapter(ArrayList<Review> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @Override
    public ReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_reviews, parent, false);
        return new ReviewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewsViewHolder holder, int position) {
        final Review currentReview = reviewsList.get(position);
        holder.tv_user_name.setText(currentReview.getUserName());
        holder.tv_review_content.setText(currentReview.getReviewContent());
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }


    public class ReviewsViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_user_name;
        private TextView tv_review_content;

        public ReviewsViewHolder(View view) {
            super(view);
            tv_user_name = (TextView) view.findViewById(R.id.tv_user_name);
            tv_review_content = (TextView) view.findViewById(R.id.tv_review_content);
        }
    }
}
