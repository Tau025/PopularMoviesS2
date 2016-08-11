package com.devtau.popularmoviess2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.model.SortBy;
import java.util.List;
/**
 * Адаптер спиннера вариантов сортировки
 * Adapter for a spinner of SortBy options
 */
public class SortBySpinnerAdapter extends ArrayAdapter<SortBy> {
    public SortBySpinnerAdapter(Context context, List<SortBy> items) {
        super(context, R.layout.spinner_item, items);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        SortBy sortBy = getItem(position);
        if (sortBy != null) {
            ((TextView) convertView).setText(sortBy.getTitle(getContext()));
        }
        return convertView;
    }
}