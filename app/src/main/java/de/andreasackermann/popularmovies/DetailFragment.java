package de.andreasackermann.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.andreasackermann.popularmovies.data.MoviesContract;
import de.andreasackermann.popularmovies.json.MovieJsonHelper;
import de.andreasackermann.popularmovies.json.ReviewJsonHelper;
import de.andreasackermann.popularmovies.json.TrailerJsonHelper;

/**
 * Created by Andreas on 07.01.2017.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";

    private static final int LOADER_MOVIES = 0;
    private static final int LOADER_REVIEWS = 1;
    private static final int LOADER_TRAILERS = 2;

    private ReviewsAdapter reviewsAdapter;
    private TrailersAdapter trailersAdapter;

    private ImageView mImageView;
    private TextView mOriginalTitleView;
    private TextView mOverviewView;
    private TextView mReleaseDateView;
    private TextView mVoteAverageView;
    private ListView mReviews;
    private ListView mTrailers;


    public DetailFragment() {
//        setHasOptionsMenu(true);
    }

    private final static String LOG_TAG = DetailFragment.class.getName();

    public static final String MOVIE_RECORD ="movie";

    private Uri mUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        reviewsAdapter = new ReviewsAdapter(getContext(), null, 0);
        trailersAdapter = new TrailersAdapter(getContext(), null, 0);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            new httpReviewFetcher().execute(MoviesContract.getMovieIdFromUri(mUri));

        }

        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        mImageView = (ImageView)root.findViewById(R.id.moviePoster);
        mOriginalTitleView = (TextView)root.findViewById(R.id.movieTitle);
        mOverviewView = (TextView)root.findViewById(R.id.overview);
        mReleaseDateView = (TextView)root.findViewById(R.id.publishDate);
        mVoteAverageView = (TextView)root.findViewById(R.id.voteAverage);
        mReviews = (ListView) root.findViewById(R.id.reviews);
        mReviews.setAdapter(reviewsAdapter);
        mTrailers = (ListView) root.findViewById(R.id.trailers);
        mTrailers.setAdapter(trailersAdapter);


        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_MOVIES, null, this);
        getLoaderManager().initLoader(LOADER_REVIEWS, null, this);
        getLoaderManager().initLoader(LOADER_TRAILERS, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            switch (id) {
                case LOADER_MOVIES:
                        return new CursorLoader(
                                getActivity(),
                                mUri,
                                null,
                                null,
                                null,
                                null
                        );

                case LOADER_REVIEWS:
                    return new CursorLoader(
                            getActivity(),
                            MoviesContract.ReviewEntry.CONTENT_URI.buildUpon().appendPath(MoviesContract.getMovieIdFromUri(mUri)).build(),
                            null,
                            null,
                            null,
                            null
                    );

                case LOADER_TRAILERS:
                    return new CursorLoader(
                            getActivity(),
                            MoviesContract.TrailerEntry.CONTENT_URI.buildUpon().appendPath(MoviesContract.getMovieIdFromUri(mUri)).build(),
                            null,
                            null,
                            null,
                            null
                    );

            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_MOVIES:
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
                break;
            case LOADER_REVIEWS:
                reviewsAdapter.swapCursor(data);
                break;
            case LOADER_TRAILERS:
                trailersAdapter.swapCursor(data);
                break;
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

    private class httpReviewFetcher extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            MovieJsonHelper h = new MovieJsonHelper(getContext());
            new ReviewJsonHelper(getActivity(), (String) params[0]).updateDb();
            new TrailerJsonHelper(getActivity(), (String) params[0]).updateDb();
            return null;
        }
    }
}
