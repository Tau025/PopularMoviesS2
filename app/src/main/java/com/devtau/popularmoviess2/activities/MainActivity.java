package com.devtau.popularmoviess2.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.devtau.popularmoviess2.adapters.MoviesListCursorAdapter;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.fragments.MovieDetailsFragment;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.util.Logger;
import java.util.GregorianCalendar;
/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailsActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MoviesListCursorAdapter.OnItemClickListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private static final int LOADER_RESULTS = 115297;
    private MoviesListCursorAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int count = getMoviesCountViaCR();
        if(count == 0) {
            populateDBViaCR();
            count = getMoviesCountViaCR();
            Logger.d(LOG_TAG, "inserted " + String.valueOf(count) + " mock movies");
        }

        adapter = new MoviesListCursorAdapter();
        adapter.setOnItemClickListener(this);
        getSupportLoaderManager().restartLoader(LOADER_RESULTS, null, this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (findViewById(R.id.movie_details_container) != null) {
            mTwoPane = true;
        }
    }

    private int getMoviesCountViaCR() {
        Cursor cursor = getContentResolver().query(MoviesTable.CONTENT_URI, null, null, null, null);
        DatabaseUtils.dumpCursor(cursor);
        int count = 0;
        if(cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    private void populateDBViaCR() {
        ContentResolver resolver = getContentResolver();

        insertToDB(resolver, new Movie(1, "title1", "posterPath1", "plotSynopsis1", 8.9,
                        new GregorianCalendar(2016, 5, 26, 14, 0)));
        insertToDB(resolver, new Movie(2, "title2", "posterPath2", "plotSynopsis2", 7.9,
                        new GregorianCalendar(2016, 5, 27, 10, 10)));
        insertToDB(resolver, new Movie(3, "title3", "posterPath3", "plotSynopsis3", 5.9,
                        new GregorianCalendar(2016, 5, 28, 8, 40)));
        insertToDB(resolver, new Movie(4, "title4", "posterPath4", "plotSynopsis4", 3.9,
                        new GregorianCalendar(2016, 6, 1, 10, 0)));
        insertToDB(resolver, new Movie(5, "title5", "posterPath5", "plotSynopsis5", 7.9,
                        new GregorianCalendar(2016, 6, 3, 12, 30)));
        insertToDB(resolver, new Movie(6, "title6", "posterPath6", "plotSynopsis6", 9.9,
                        new GregorianCalendar(2016, 6, 5, 9, 20)));
    }

    private void insertToDB(ContentResolver resolver, Movie movie) {
        resolver.insert(MoviesTable.CONTENT_URI, MoviesTable.getContentValues(movie));
    }




    @Override
    public void onItemClicked(Cursor cursor) {
        long movieId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        Logger.v(LOG_TAG, "mTwoPane: " + String.valueOf(mTwoPane));
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putLong(MovieDetailsFragment.MOVIE_ID_EXTRA, movieId);
            MovieDetailsFragment fragment = new MovieDetailsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
            intent.putExtra(MovieDetailsFragment.MOVIE_ID_EXTRA, movieId);
            startActivity(intent);
        }
    }


    //LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_RESULTS:
                return new CursorLoader(this, MoviesTable.CONTENT_URI, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_RESULTS:
                this.adapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_RESULTS:
                this.adapter.swapCursor(null);
                break;
        }
    }
}
