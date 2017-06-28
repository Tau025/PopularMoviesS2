package com.devtau.popularmoviess2.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.model.Movie;

public class HeaderFooterAdapter extends RecyclerViewCursorAdapter<HeaderFooterAdapter.ViewHolder>{
    
    private ListItemClickListener listener;
    private View headerView;
    
    
    public HeaderFooterAdapter(Cursor cursor, ListItemClickListener listener){
        super(cursor);
        this.listener = listener;
    }
    
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_HEADER) {
            //inflate header
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movies, parent, false);
            headerView = view;
        } else if (viewType == TYPE_REGULAR_ITEM) {
            //inflate regular item
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movies, parent, false);
        } else {
            //inflate footer
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movies, parent, false);
        }
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolderCursor(ViewHolder holder, Cursor cursor, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            //bind header
        } else if (getItemViewType(position) == TYPE_FOOTER) {
            //bind footer
        } else {
            //bind regular item
            cursor.moveToPosition(position - 1);
            final Movie current = new Movie(cursor);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onListItemClicked(current);
                }
            });
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == getItemCount() - 1) return TYPE_FOOTER;
        return TYPE_REGULAR_ITEM;
    }
    
    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        if (count > 0) {
            return count + 2;
        } else {
            return 0;
        }
    }
    
    public View getHeaderView() {
        return headerView;
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
        void onListItemClicked(Movie selected);
    }
}
