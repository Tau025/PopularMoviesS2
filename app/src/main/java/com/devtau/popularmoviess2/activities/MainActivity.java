package com.devtau.popularmoviess2.activities;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.devtau.popularmoviess2.adapters.MoviesListCursorAdapter;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.fragments.NoInternetDF;
import com.devtau.popularmoviess2.fragments.ProgressBarDF;
import com.devtau.popularmoviess2.presenters.MoviesListPresenter;
import com.devtau.popularmoviess2.view.MoviesListViewInterface;
/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailsActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MainActivity extends AppCompatActivity implements
        MoviesListCursorAdapter.OnItemClickListener,
        MoviesListViewInterface {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private MoviesListCursorAdapter rvAdapter;
    private MoviesListPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_details_container) != null) {
            mTwoPane = true;
        }
        initList();
        presenter = new MoviesListPresenter(this);
        presenter.sendRequestToServer();
        presenter.restartLoader();
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
                item.setTitle(presenter.toggleSortOrder());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(Cursor cursor) {
        presenter.onListItemClick(cursor, mTwoPane);
    }


    @Override
    public boolean showNoInternetDF() {
        return NoInternetDF.show(getSupportFragmentManager(), presenter);
    }

    @Override
    public boolean showProgressBarDF() {
        return ProgressBarDF.show(getSupportFragmentManager());
    }

    @Override
    public boolean dismissProgressBarDF() {
        return ProgressBarDF.dismiss(getSupportFragmentManager());
    }

    @Override
    public void initList() {
        rvAdapter = new MoviesListCursorAdapter(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, mTwoPane ? 3 : 2));
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void swapCursor(Cursor cursor) {
        rvAdapter.swapCursor(cursor);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
