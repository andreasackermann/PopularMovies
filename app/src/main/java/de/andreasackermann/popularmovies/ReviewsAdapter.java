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

public class ReviewsAdapter extends CursorAdapter {

    public ReviewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.review_row, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView authorView = (TextView) view.findViewById(R.id.reviewAuthor);
        authorView.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_AUTHOR)));

        TextView contentView = (TextView) view.findViewById(R.id.reviewContent);
        contentView.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT)));
    }
}
