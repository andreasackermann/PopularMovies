package de.andreasackermann.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {

    private ArrayList movies = new ArrayList<MovieRecord>();

    private MoviesAdapter moviesAdapter;

    private GridView thumbnails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = (View)getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(view);


        if (savedInstanceState != null) {
            movies = savedInstanceState.getParcelableArrayList("movies");
        }
        moviesAdapter = new MoviesAdapter(this, movies);

        thumbnails = (GridView) findViewById(R.id.thumbnails);
        thumbnails.setAdapter(moviesAdapter);
        thumbnails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieRecord clicked = (MovieRecord) parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("movie", clicked);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("movies", movies);
    }

    @Override
    protected void onResume() {
        loadMovies();
        super.onResume();
    }

    public void loadMovies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String order = prefs.getString(getString(R.string.pref_order_key),getString(R.string.pref_order_default));
        new GetMoviesList().execute(order);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
                        .make(thumbnails, MainActivity.this.getString(R.string.warn_no_internet), Snackbar.LENGTH_INDEFINITE)
                        .setAction(MainActivity.this.getString(R.string.warn_button_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MainActivity.this.loadMovies();
                            }
                        });

                snackbar.show();
            }
            moviesAdapter.notifyDataSetChanged();
        }
    }
}
