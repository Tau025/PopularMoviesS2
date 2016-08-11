package com.devtau.popularmoviess2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.model.Trailer;
import com.devtau.popularmoviess2.utility.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;
/**
 * Адаптер списка трейлеров
 * Adapter for a list of Trailers
 */
public class TrailersListAdapter extends ArrayAdapter<Trailer> {
    public TrailersListAdapter(Context context, int resource, List<Trailer> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_trailers, parent, false);
        }

        Trailer trailer = getItem(position);

        if (trailer != null) {
            ImageView iv_trailer_thumb = (ImageView) convertView.findViewById(R.id.iv_trailer_thumb);
            String trailerThumbPath = Constants.YOUTUBE_TRAILER_THUMB.replace("$", trailer.getSource());
            Picasso.with(getContext())
                    .load(trailerThumbPath)
                    .error(android.R.drawable.ic_media_play)
                    .centerCrop()
                    .resize(128, 72)
                    .into(iv_trailer_thumb);

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            TextView tv_type_size = (TextView) convertView.findViewById(R.id.tv_type_size);

            if (tv_name != null && tv_type_size != null) {
                tv_name.setText(trailer.getName());
                tv_type_size.setText(trailer.getTypeSize());
            }
        }
        return convertView;
    }
}
