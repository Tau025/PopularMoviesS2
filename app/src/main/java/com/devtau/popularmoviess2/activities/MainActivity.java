package com.devtau.popularmoviess2.activities;

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
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import com.devtau.popularmoviess2.adapters.MoviesListCursorAdapter;
import com.devtau.popularmoviess2.database.MoviesTable;
import com.devtau.popularmoviess2.fragments.MovieDetailsFragment;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.model.SortBy;
import com.devtau.popularmoviess2.sync.SyncAdapter;
import com.devtau.popularmoviess2.utility.Constants;
import com.devtau.popularmoviess2.utility.Logger;
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
        MoviesListCursorAdapter.OnItemClickListener,
        SyncAdapter.SyncAdapterListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private static final int LOADER_RESULTS = 115297;
    private MoviesListCursorAdapter rvAdapter;
    private SortBy sortBy = Constants.DEFAULT_SORT_BY;
    private int imageWidth, imageHeight;
    private int columnCount = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calculateParamsOfThumb();

        rvAdapter = new MoviesListCursorAdapter(this, imageWidth, imageHeight);
        getSupportLoaderManager().restartLoader(LOADER_RESULTS, null, this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

        if (findViewById(R.id.movie_details_container) != null) {
            mTwoPane = true;
        }
        SyncAdapter.initializeSyncAdapter(this);
    }

    private void calculateParamsOfThumb() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        imageWidth = metrics.widthPixels / columnCount;
        imageHeight = Math.round(((float) imageWidth /
                Constants.DEFAULT_POSTER_WIDTH) * Constants.DEFAULT_POSTER_HEIGHT);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_order_toggle:
                sortBy = SortBy.toggle(sortBy);
                item.setTitle(sortBy.getDescription(this));
                SyncAdapter.syncImmediately(this);
                getSupportLoaderManager().restartLoader(LOADER_RESULTS, null, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(Cursor cursor) {
        long movieId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        Logger.v(LOG_TAG, "mTwoPane: " + String.valueOf(mTwoPane));
        DatabaseUtils.dumpCursor(cursor);
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
                String sortOrder = sortBy.getDatabaseID() + " DESC";
                return new CursorLoader(this, MoviesTable.CONTENT_URI, null, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_RESULTS:
                this.rvAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_RESULTS:
                this.rvAdapter.swapCursor(null);
                break;
        }
    }

    //SyncAdapterListener
    @Override
    public SortBy getSortBy() {
        return sortBy;
    }
}
