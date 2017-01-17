package de.andreasackermann.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.andreasackermann.popularmovies.data.MoviesContract;

/**
 * Created by Andreas on 07.01.2017.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";

    private ImageView mImageView;
    private TextView mOriginalTitleView;
    private TextView mOverviewView;
    private TextView mReleaseDateView;
    private TextView mVoteAverageView;


    public DetailFragment() {
//        setHasOptionsMenu(true);
    }

    private final static String LOG_TAG = DetailFragment.class.getName();

    public static final String MOVIE_RECORD ="movie";

    private Uri mUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        mImageView = (ImageView)root.findViewById(R.id.moviePoster);
        mOriginalTitleView = (TextView)root.findViewById(R.id.movieTitle);
        mOverviewView = (TextView)root.findViewById(R.id.overview);
        mReleaseDateView = (TextView)root.findViewById(R.id.publishDate);
        mVoteAverageView = (TextView)root.findViewById(R.id.voteAverage);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    null,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String path = data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_IMAGE));
            Picasso picasso = Picasso.with(getContext());
            picasso.load(new File(path)).into(mImageView);

            String originalTitle = data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE));
            mOriginalTitleView.setText(originalTitle);

            String overview = data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_OVERVIEW));
            mOverviewView.setText(overview);

            String releaseDate = formatReleaseDate(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASED)));
            mReleaseDateView.setText(releaseDate);

            String voteAvg = formatVoteAverage(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_AVG)));
            mVoteAverageView.setText(voteAvg);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

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
