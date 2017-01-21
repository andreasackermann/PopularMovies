package de.andreasackermann.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.andreasackermann.popularmovies.data.MoviesContract;

/**
 * Created by Andreas on 17.01.2017.
 */

public class TrailersAdapter extends CursorAdapter {

    public TrailersAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.trailer_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView authorView = (TextView) view.findViewById(R.id.trailerName);
        authorView.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_NAME)));

//        TextView contentView = (TextView) view.findViewById(R.id.trailerKey);
//        contentView.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_KEY)));
    }
}
