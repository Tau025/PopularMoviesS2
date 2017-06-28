package com.devtau.popularmoviess2.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
/**
 * Поскольку в приложении может быть несколько списков, каждому будет соответствовать свой адаптер
 * и все они должны наследовать от этого класса
 * For an app can have several lists, each one should have their own adapter that will extend this one
 */
public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    
    public static final int TYPE_HEADER = 1586;
    static final int TYPE_REGULAR_ITEM = 1587;
    static final int TYPE_FOOTER = 1588;
    public static final String LOG_TAG = "CursorRVAdapter";
    private Cursor cursor;
    
    
    RecyclerViewCursorAdapter() {
    }
    
    RecyclerViewCursorAdapter(Cursor cursor) {
        this.cursor = cursor;
    }
    
    
    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? this.cursor.getCount() : 0;
    }

    //Метод вернет курсор, выставленный в выбранную строку
    //Provides single row from cursor tied to this list
    public Cursor getItem(int position) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(position);
        }
        return cursor;
    }

    //Метод вернет весь курсор связанного списка
    //Provides the whole cursor tied to this list
    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        Cursor cursor = getItem(position);
        onBindViewHolderCursor(holder, cursor, position);
    }

    public abstract void onBindViewHolderCursor(VH holder, Cursor cursor, int position);
}
