package de.andreasackermann.popularmovies;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Andreas on 03.12.2016.
 */

public class DetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        MovieRecord movie = (MovieRecord)(getIntent().getParcelableExtra("movie"));

        Bundle arguments = new Bundle();
        arguments.putParcelable(DetailFragment.MOVIE_RECORD, movie);

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment)
                .commit();
    }
}
