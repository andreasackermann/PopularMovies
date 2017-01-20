package de.andreasackermann.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import de.andreasackermann.popularmovies.data.MoviesContract;
import de.andreasackermann.popularmovies.json.MovieJsonHelper;


/**
 * Created by Andreas on 07.01.2017.
 */

public class ThumbnailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = ThumbnailsFragment.class.getName();

    public static final String SQLITE_TRUE = "1";

    private ThumbnailAdapter thumbnailAdapter;

    private GridView thumbnails;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        thumbnailAdapter = new ThumbnailAdapter(getActivity(), null, 0);

        View view = inflater.inflate(R.layout.fragment_thumbnail, container, false);
        thumbnails = (GridView) view.findViewById(R.id.thumbnails);
        thumbnails.setAdapter(thumbnailAdapter);

        thumbnails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String movieId = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry._ID));
                    if (getResources().getBoolean(R.bool.onePane)) {
                        Intent intent = new Intent(getContext(), DetailActivity.class);
                        intent.setData(MoviesContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).build());
                        startActivity(intent);
                    } else {
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(DetailFragment.DETAIL_URI, MoviesContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).build());

                        DetailFragment fragment = new DetailFragment();
                        fragment.setArguments(arguments);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        new httpFetcher().execute();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String order = prefs.getString(getString(R.string.pref_order_key),getString(R.string.pref_order_default));
        String sortOrder;
        String whereCondition;

        if (order.equals(getContext().getResources().getString(R.string.value_order_popular))) {
            sortOrder = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";
            whereCondition = MoviesContract.MovieEntry.COLUMN_CAT_POPULAR + " = ? ";
        } else if (order.equals(getContext().getResources().getString(R.string.value_order_top_rated))) {
            sortOrder = MoviesContract.MovieEntry.COLUMN_VOTE_AVG + " DESC";
            whereCondition = MoviesContract.MovieEntry.COLUMN_CAT_TOP_RATED + " = ? ";
        } else if (order.equals(getContext().getResources().getString(R.string.value_order_favorites))) {
            /*
             * https://review.udacity.com/#!/rubrics/67/view
             * When the "favorites" setting option is selected, the main view displays the entire favorites
             * collection based on movie ids stored in the ContentProvider
             */
            sortOrder = MoviesContract.MovieEntry._ID + " ASC";
            whereCondition = MoviesContract.MovieEntry.COLUMN_CAT_FAVORITE + " = ? ";
        }
        else {
            throw new UnsupportedOperationException("Unknown sort order " + order);
        }

        return new CursorLoader(
                getActivity(),
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                whereCondition,
                new String[] { SQLITE_TRUE },
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");
        if (data.getCount() > 0) {
            Log.d(LOG_TAG, "Cursor count = " + data.getCount());
            thumbnailAdapter.swapCursor(data);

        } else {
            Snackbar snackbar = Snackbar
                    .make(thumbnails, ThumbnailsFragment.this.getString(R.string.warn_no_internet), Snackbar.LENGTH_LONG)
                    .setAction(ThumbnailsFragment.this.getString(R.string.warn_button_retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // todo implement
                        }
                    });

            snackbar.show();
        }
        // TODO scoll?
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
        thumbnailAdapter.swapCursor(null);
    }

    private class httpFetcher extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String order = prefs.getString(getString(R.string.pref_order_key),getString(R.string.pref_order_default));

            MovieJsonHelper h = new MovieJsonHelper(getContext());
            h.updateDb();
            return null;
        }
    }
}
