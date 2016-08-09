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
    private Cursor cursor;

    public void swapCursor(final Cursor cursor) {
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
        onBindViewHolder(holder, cursor);
    }

    public abstract void onBindViewHolder(VH holder, Cursor cursor);
}
