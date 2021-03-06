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

    private ThumbnailAdapter thumbnailAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_thumbnail, container, false);
        GridView mThumbnails = (GridView) view.findViewById(R.id.thumbnails);
        if (thumbnailAdapter == null) {
            thumbnailAdapter = new ThumbnailAdapter(getActivity(), null, 0);
        }
        mThumbnails.setAdapter(thumbnailAdapter);
        mThumbnails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String order = prefs.getString(getContext().getString(R.string.pref_order_key),getContext().getString(R.string.pref_order_default));
        if (!order.equals(getString(R.string.value_order_favorites))) {
            // Favorite entries are taken only from local db
            new HttpFetcher().execute(order);
        }
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
                new String[] { "1" },
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        thumbnailAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        thumbnailAdapter.swapCursor(null);
    }

    private class HttpFetcher extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String[] params) {
            MovieJsonHelper h = new MovieJsonHelper(getContext(), params[0]);
            return h.updateDb();
        }

        @Override
        protected void onPostExecute(Boolean online) {
            if (!online && ThumbnailsFragment.this.getView()!=null) {
                Snackbar snackbar = Snackbar
                        .make(ThumbnailsFragment.this.getView(),
                                ThumbnailsFragment.this.getString(R.string.warn_no_internet),
                                Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }
}
