package de.andreasackermann.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andreas on 07.01.2017.
 */

public class DetailFragment extends Fragment {

    public DetailFragment() {
//        setHasOptionsMenu(true);
    }

    public static final String MOVIE_RECORD ="movie";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            MovieRecord movie = (MovieRecord)arguments.getParcelable(DetailFragment.MOVIE_RECORD);
            ImageView imageView = (ImageView)root.findViewById(R.id.moviePoster) ;
            Picasso picasso = Picasso.with(getContext());
            picasso.load(movie.getMoviePosterImageThumbnail()).into(imageView);

            ((TextView)root.findViewById(R.id.movieTitle)).setText(movie.getOriginalTitle());
            ((TextView)root.findViewById(R.id.overview)).setText(movie.getOverview());
            ((TextView)root.findViewById(R.id.publishDate)).setText(formatReleaseDate(movie.getReleaseDate()));
            ((TextView)root.findViewById(R.id.voteAverage)).setText(formatVoteAverage(movie.getVoteAverage()));
        }
        return root;
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
