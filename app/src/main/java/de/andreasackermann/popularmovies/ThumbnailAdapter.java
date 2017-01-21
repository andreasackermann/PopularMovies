package de.andreasackermann.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import de.andreasackermann.popularmovies.data.MoviesContract;

/**
 * Created by Andreas on 14.01.2017.
 */

public class ThumbnailAdapter extends CursorAdapter {

    public ThumbnailAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.movie_cell, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView)view.findViewById(R.id.movieCell);
        String path = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_IMAGE));
        Picasso picasso = Picasso.with(context);
        picasso.load(new File(path)).into(imageView);
    }
}
