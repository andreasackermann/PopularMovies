package de.andreasackermann.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private ShareActionProvider mShareActionProvider;

    private LinearLayout mReviewsContainer;
    private LinearLayout mTrailersContainer;
    private ImageView mImageView;
    private ImageView mFavoriteToggle;
    private TextView mOriginalTitleView;
    private TextView mOverviewView;
    private TextView mReleaseDateView;
    private TextView mVoteAverageView;

    private Uri mUri;
    private HttpFetcher mHttpFetcher;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_MOVIES, null, this);
        getLoaderManager().initLoader(LOADER_REVIEWS, null, this);
        getLoaderManager().initLoader(LOADER_TRAILERS, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = null;
        Bundle arguments = getArguments();
        if (arguments != null) {
            root = inflater.inflate(R.layout.fragment_detail, container, false);
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            Log.d(LOG_TAG, "onCreateView:mUri = " + mUri);
            mHttpFetcher = new HttpFetcher();
            mHttpFetcher.execute(MoviesContract.getMovieIdFromUri(mUri));

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
        return root;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mHttpFetcher!=null) {
            this.mHttpFetcher.cancel(true);
        }
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
                        mFavoriteToggle.setContentDescription(getString(R.string.action_unmark_favorite));
                    } else {
                        mFavoriteToggle.setImageResource(R.drawable.ic_favorite_black_24px);
                        mFavoriteToggle.setContentDescription(getString(R.string.action_mark_favorite));
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
                Log.d(LOG_TAG, "data.getCount()=" + data.getCount());
                mReviewsContainer.removeAllViews();
                if (data.getCount()>0) {
                    while (data.moveToNext()) {
                        View row = LayoutInflater.from(getActivity()).inflate(R.layout.review_row, null);
                        ((TextView) row.findViewById(R.id.reviewAuthor)).setText(data.getString(data.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_AUTHOR)));
                        ((TextView) row.findViewById(R.id.reviewContent)).setText(data.getString(data.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT)));
                        mReviewsContainer.addView(row);
                    }
                } else {
                    TextView noResult = new TextView(getContext());
                    noResult.setText(getString(R.string.label_reviews_unavailable));
                    mReviewsContainer.addView(noResult);
                }
                break;
            case LOADER_TRAILERS:
                mTrailersContainer.removeAllViews();
                if (data.getCount()>0) {
                    while (data.moveToNext()) {
                        View row = LayoutInflater.from(getActivity()).inflate(R.layout.trailer_row, null);

                        final String url = getString(R.string.youtube_base_url) + data.getString(data.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_KEY));
                        row.findViewById(R.id.trailerPlayIcon).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                watchYoutubeVideo(url);
                            }
                        });
                        row.findViewById(R.id.trailerShareIcon).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                shareYoutubeVideo(url);
                            }
                        });
                        if (data.isFirst()) {
                            if (mShareActionProvider!=null) {
                                mShareActionProvider.setShareIntent(getShareYouTubeVideoIntent(url));
                            }
                        }
                        ((TextView) row.findViewById(R.id.trailerName)).setText(data.getString(data.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_NAME)));
                        mTrailersContainer.addView(row);
                    }
                } else {
                    TextView noResult = new TextView(getContext());
                    noResult.setText(getString(R.string.label_trailers_unavailable));
                    mTrailersContainer.addView(noResult);
                }
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

    private class HttpFetcher extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String[] params) {
            return new ReviewJsonHelper(getActivity(), params[0]).updateDb()
                    && new TrailerJsonHelper(getActivity(), params[0]).updateDb();
        }

        @Override
        protected void onPostExecute(Boolean online) {
            if (!this.isCancelled() && !online) {
                Snackbar snackbar = Snackbar
                        .make(DetailFragment.this.getView(), DetailFragment.this.getString(R.string.warn_no_internet), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    private void watchYoutubeVideo(String url){
        Intent watchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(watchIntent);
    }

    private Intent getShareYouTubeVideoIntent(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.intro_share) + url);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    private void shareYoutubeVideo(String url){
        startActivity(getShareYouTubeVideoIntent(url));
    }


}
