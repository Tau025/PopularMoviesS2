package com.devtau.popularmoviess2.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.utility.Utility;

public class MoviesListCursorAdapter extends RecyclerViewCursorAdapter<MoviesListCursorAdapter.ViewHolder>
        implements View.OnClickListener {
    private OnItemClickListener listener;
    private int imageWidth, imageHeight;

    public MoviesListCursorAdapter(OnItemClickListener listener, int imageWidth, int imageHeight) {
        this.listener = listener;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        String posterPath = cursor.getString(cursor.getColumnIndex(MoviesTable.POSTER_PATH));
        Utility.loadImageToView(holder.view.getContext(), posterPath, holder.movieThumb, imageWidth, imageHeight);
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            RecyclerView recyclerView = (RecyclerView) view.getParent();
            int position = recyclerView.getChildLayoutPosition(view);
            if (position != RecyclerView.NO_POSITION) {
                Cursor cursor = getItem(position);
                listener.onItemClicked(cursor);
            }
        }
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ImageView movieThumb;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            movieThumb = (ImageView) view.findViewById(R.id.movie_thumb);
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(Cursor cursor);
    }
}