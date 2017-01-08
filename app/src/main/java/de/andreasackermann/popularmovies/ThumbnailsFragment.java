package de.andreasackermann.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Andreas on 07.01.2017.
 */

public class ThumbnailsFragment extends Fragment {

    private final static String LOG_TAG = ThumbnailsFragment.class.getName();

    private ArrayList movies = new ArrayList<MovieRecord>();

    private MoviesAdapter moviesAdapter;

    private GridView thumbnails;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        if (savedInstanceState != null) {
            movies = savedInstanceState.getParcelableArrayList("movies");
        }
        moviesAdapter = new MoviesAdapter(this, movies);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thumbnail, container, false);
        thumbnails = (GridView) view.findViewById(R.id.thumbnails);
        thumbnails.setAdapter(moviesAdapter);
        thumbnails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieRecord clicked = (MovieRecord) parent.getAdapter().getItem(position);
                if (getResources().getBoolean(R.bool.onePane)) {
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    intent.putExtra("movie", clicked);
                    startActivity(intent);
                } else {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(DetailFragment.MOVIE_RECORD, clicked);

                    DetailFragment fragment = new DetailFragment();
                    fragment.setArguments(arguments);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
                }
            }
        });
        return view;
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
    public void onResume() {
        loadMovies();
        super.onResume();
    }

    public void loadMovies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String order = prefs.getString(getString(R.string.pref_order_key),getString(R.string.pref_order_default));
        new GetMoviesList().execute(order);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("movies", movies);
    }

    public class GetMoviesList extends AsyncTask<String, Void, ArrayList<MovieRecord>> {

        public static final String API_VERSION = "3";
        public static final String API_ENDPOINT = "api.themoviedb.org";
        public static final String API_KEY = "api_key";
        public static final String PROTOCOL = "https";
        public static final String MOVIE = "movie";
        private final String LOG_TAG= GetMoviesList.class.getSimpleName();

        /**
         * solution from
         * http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
         * as suggested in implementation guide
         */
        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        @Override
        protected ArrayList<MovieRecord> doInBackground(@NotNull String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            if (isOnline()) {

                try {
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme(PROTOCOL);
                    builder.authority(API_ENDPOINT);
                    builder.appendPath(API_VERSION);
                    builder.appendPath(MOVIE);
                    builder.appendPath(params[0]);
                    builder.appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY);
                    Uri uri = builder.build();

                    URL url = new URL(uri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();

                    if (inputStream != null) {
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        StringBuffer sb = new StringBuffer();
                        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                            sb.append(line);
                        }
                        /*
                         * According to https://discussions.udacity.com/t/api-only-returns-20-movies-is-that-right/32048
                         * we do not have to do any paging, but can work with the 20 titles returned with page 1
                         */
                        return MovieRecord.Parser.parse(sb.toString());
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {}
                    }
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieRecord> m) {
            movies.clear();
            if (m != null) {
                movies.addAll(m);
            } else {
                Snackbar snackbar = Snackbar
                        .make(thumbnails, ThumbnailsFragment.this.getString(R.string.warn_no_internet), Snackbar.LENGTH_INDEFINITE)
                        .setAction(ThumbnailsFragment.this.getString(R.string.warn_button_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ThumbnailsFragment.this.loadMovies();
                            }
                        });

                snackbar.show();
            }
            moviesAdapter.notifyDataSetChanged();
        }
    }
}
