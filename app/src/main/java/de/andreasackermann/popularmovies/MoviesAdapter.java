package de.andreasackermann.popularmovies;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Andreas on 03.12.2016.
 */

public class MoviesAdapter extends ArrayAdapter<MovieRecord> {

    private final String LOG_TAG= MoviesAdapter.class.getSimpleName();

    public MoviesAdapter(Activity context, List<MovieRecord> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieRecord movie = (MovieRecord) getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_cell, parent, false);
        }
        ImageView imageView = (ImageView)convertView.findViewById(R.id.movieCell);

        Picasso picasso = Picasso.with(getContext());
        picasso.load(movie.getMoviePosterImageThumbnail()).into(imageView);

        return convertView;
    }
}
