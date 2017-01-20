package de.andreasackermann.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.andreasackermann.popularmovies.data.MoviesContract;
import de.andreasackermann.popularmovies.json.MovieJsonHelper;
import de.andreasackermann.popularmovies.json.ReviewJsonHelper;
import de.andreasackermann.popularmovies.json.TrailerJsonHelper;

/**
 * Created by Andreas on 07.01.2017.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";

    private static final  String LOG_TAG = DetailFragment.class.getName();

    private static final int LOADER_MOVIES = 0;
    private static final int LOADER_REVIEWS = 1;
    private static final int LOADER_TRAILERS = 2;

    private LinearLayout mReviewsContainer;
    private LinearLayout mTrailersContainer;
    private ImageView mImageView;
    private ImageView mFavoriteToggle;
    private TextView mOriginalTitleView;
    private TextView mOverviewView;
    private TextView mReleaseDateView;
    private TextView mVoteAverageView;

    private Uri mUri;

    public DetailFragment() {
//        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            new httpFetcher().execute(MoviesContract.getMovieIdFromUri(mUri));

            View root = inflater.inflate(R.layout.fragment_detail, container, false);
            mImageView = (ImageView)root.findViewById(R.id.moviePoster);
            mOriginalTitleView = (TextView)root.findViewById(R.id.movieTitle);
            mOverviewView = (TextView)root.findViewById(R.id.overview);
            mReleaseDateView = (TextView)root.findViewById(R.id.publishDate);
            mVoteAverageView = (TextView)root.findViewById(R.id.voteAverage);
            mReviewsContainer = (LinearLayout) root.findViewById(R.id.reviews);
            mTrailersContainer = (LinearLayout) root.findViewById(R.id.trailers);
            mFavoriteToggle = (ImageView) root.findViewById(R.id.toggleFavorite);
            return root;
        }
        return null;

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
        Log.d(LOG_TAG, "onLoadFinished id=" + loader.getId());
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

                    final int isFavoriteAction = data.getInt(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_CAT_FAVORITE))==0?1:0;
                    if (isFavoriteAction==0) {
                        mFavoriteToggle.setImageResource(R.drawable.ic_favorite_border_black_24px);
                    } else {
                        mFavoriteToggle.setImageResource(R.drawable.ic_favorite_black_24px);
                    }
                    mFavoriteToggle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ContentValues val = new ContentValues();
                            val.put(MoviesContract.MovieEntry.COLUMN_CAT_FAVORITE, isFavoriteAction);
                            Log.d(LOG_TAG, "updating " + mUri.toString());
                            getContext().getContentResolver().update(mUri,val,null,null);
                        }
                    });

                }
                break;
            case LOADER_REVIEWS:
                mReviewsContainer.removeAllViews();
                try {
                    data.moveToFirst();
                    do {
                        View row = LayoutInflater.from(getActivity()).inflate(R.layout.review_row, null);
                        ((TextView) row.findViewById(R.id.reviewAuthor)).setText(data.getString(data.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_AUTHOR)));
                        ((TextView) row.findViewById(R.id.reviewContent)).setText(data.getString(data.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT)));
                        mReviewsContainer.addView(row);
                    } while (data.moveToNext());
                } catch (Exception e) {}
                break;
            case LOADER_TRAILERS:
                mTrailersContainer.removeAllViews();
                try {
                    data.moveToFirst();
                    do {

                        View row = LayoutInflater.from(getActivity()).inflate(R.layout.trailer_row, null);
                        ((TextView) row.findViewById(R.id.trailerName)).setText(data.getString(data.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_NAME)));
                        final String key = data.getString(data.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_KEY));
                        ((ImageView) row.findViewById(R.id.trailerPlayIcon)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                watchYoutubeVideo(key);
                            }
                        });
                        mTrailersContainer.addView(row);
                    } while (data.moveToNext());
                } catch (Exception e) {}
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

    private class httpFetcher extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            MovieJsonHelper h = new MovieJsonHelper(getContext());
            new ReviewJsonHelper(getActivity(), (String) params[0]).updateDb();
            new TrailerJsonHelper(getActivity(), (String) params[0]).updateDb();
            return null;
        }
    }


    /*
     * Solution from http://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
     */
    public void watchYoutubeVideo(String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }
}
