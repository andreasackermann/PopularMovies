package de.andreasackermann.popularmovies.json;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import de.andreasackermann.popularmovies.BuildConfig;
import de.andreasackermann.popularmovies.data.MoviesContract;

/**
 * Created by Andreas on 15.01.2017.
 */

public class MovieJsonHelper extends JsonHelper {

    private final static String LOG_TAG = MovieJsonHelper.class.getName();

    @Override
    protected Uri getUri() {
        return MoviesContract.MovieEntry.CONTENT_URI;
    }

    public MovieJsonHelper(Context context) { //, String movieId
        super(context);
//        httpUriBuilder.appendPath(movieId);
        httpUriBuilder.appendPath("popular"); // TODO split
        httpUriBuilder.appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY);
    }

    @Override
    protected Vector<ContentValues> parseInput(String jsonInput) {
        Vector<ContentValues> cVValues = new Vector<ContentValues>();
        try {
            JSONObject jsonObject = new JSONObject(jsonInput);
            JSONArray records = jsonObject.getJSONArray("results");
            for (int i=0; i<records.length(); i++) {
                JSONObject record = (JSONObject)records.get(i);
                String title = record.getString("title");
                String posterPath = record.getString("poster_path");
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority("image.tmdb.org");
                builder.appendPath("t");
                builder.appendPath("p");
                builder.appendPath("w185"); //size
                builder.appendEncodedPath(posterPath);

                String overview = record.getString("overview");
                String voteAverage = record.getString("vote_average");
                String releaseDate = record.getString("release_date");

                ContentValues val = new ContentValues();
                val.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, title);
                val.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
                val.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, voteAverage);
                val.put(MoviesContract.MovieEntry.COLUMN_RELEASED, releaseDate);
                val.put(MoviesContract.MovieEntry.COLUMN_IMAGE, builder.toString()); // TODO Store Image itself

                cVValues.add(val);
            }
            Log.d(LOG_TAG, "Got " + records.length() + " records!");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to interpret response", e);
        }
        return cVValues;

    }




}
