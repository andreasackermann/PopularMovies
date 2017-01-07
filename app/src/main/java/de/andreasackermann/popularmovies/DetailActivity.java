package de.andreasackermann.popularmovies;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andreas on 03.12.2016.
 */

public class DetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MovieRecord movie = (MovieRecord)(getIntent().getParcelableExtra("movie"));
        setContentView(R.layout.activity_detail);

        ImageView imageView = (ImageView)findViewById(R.id.moviePoster) ;

        Picasso picasso = Picasso.with(this);
        picasso.load(movie.getMoviePosterImageThumbnail()).into(imageView);

        ((TextView)findViewById(R.id.movieTitle)).setText(movie.getOriginalTitle());
        ((TextView)findViewById(R.id.overview)).setText(movie.getOverview());
        ((TextView)findViewById(R.id.publishDate)).setText(formatReleaseDate(movie.getReleaseDate()));
        ((TextView)findViewById(R.id.voteAverage)).setText(formatVoteAverage(movie.getVoteAverage()));
    }

    private String formatReleaseDate(String unformatted) {
        DateFormat parser = new SimpleDateFormat("yyyy-mm-dd");
        DateFormat formatter = new SimpleDateFormat("yyyy");
        try {
            Date date = parser.parse(unformatted);
            return formatter.format(date);
        } catch (ParseException pe) {
            return unformatted; // better than not returning anything
        }
    }

    private String formatVoteAverage(String voteAverage) {
        return voteAverage + "/10";
    }
}
