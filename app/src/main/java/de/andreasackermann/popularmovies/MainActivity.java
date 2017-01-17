package de.andreasackermann.popularmovies;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import de.andreasackermann.popularmovies.json.MovieJsonHelper;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(view);

        if (!getResources().getBoolean(R.bool.onePane)) {
            // We're dealing with multi pane layout
            // Set detail fragment
            Log.d(LOG_TAG, "Multi pane mode");

            if (savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment())
                        .commit();
            }
        }
    }
}
