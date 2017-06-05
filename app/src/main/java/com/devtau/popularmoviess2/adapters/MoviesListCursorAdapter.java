package com.devtau.popularmoviess2.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.util.AppUtils;
/**
 * Адаптер списка фильмов
 * Adapter for a list of movies
 */
public class MoviesListCursorAdapter extends RecyclerViewCursorAdapter<MoviesListCursorAdapter.ViewHolder>
        implements View.OnClickListener {
    private ListItemClickListener listener;

    public MoviesListCursorAdapter(ListItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_movies, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        String posterPath = cursor.getString(cursor.getColumnIndex(MoviesTable.POSTER_PATH));
        AppUtils.setPosterImage(holder.movieThumb, posterPath);
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            RecyclerView recyclerView = (RecyclerView) view.getParent();
            int position = recyclerView.getChildLayoutPosition(view);
            if (position != RecyclerView.NO_POSITION) {
                Cursor cursor = getItem(position);
                listener.onListItemClicked(cursor);
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

    public interface ListItemClickListener {
        void onListItemClicked(Cursor cursor);
    }
}