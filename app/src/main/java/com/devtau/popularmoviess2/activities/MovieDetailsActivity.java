package com.devtau.popularmoviess2.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.devtau.popularmoviess2.fragments.MovieDetailsFragment;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.util.Constants;
import com.devtau.popularmoviess2.util.Logger;
/**
 * Активность, содержащая контейнер для фрагмента {@link MovieDetailsFragment}
 * Используется только если места на экране устройства недостаточно чтобы показать подробности
 * по фильму на одном экране со списком.
 *
 * An activity representing a single Movie detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 */
public class MovieDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.v(LOG_TAG, "in onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        //Если savedInstanceState не null, создававшийся ранее фрагмент будет автоматически
        //добавлен в разметку
        //If savedInstanceState is not null, the fragment will automatically be re-added
        //to its container
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(Constants.MOVIE_ID_EXTRA,
                    getIntent().getLongExtra(Constants.MOVIE_ID_EXTRA, 0));
            MovieDetailsFragment fragment = new MovieDetailsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_details_container, fragment)
                    .commit();
        }
    }
}
