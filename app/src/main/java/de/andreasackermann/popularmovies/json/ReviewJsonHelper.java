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

public class ReviewJsonHelper extends JsonHelper {

    private final static String LOG_TAG = ReviewJsonHelper.class.getName();

    public ReviewJsonHelper(Context context, String movieId) {
        super(context);
        httpUriBuilder.appendPath(movieId);
        httpUriBuilder.appendPath("reviews");
        httpUriBuilder.appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY);
    }


    @Override
    protected Vector<ContentValues> parseInput(String jsonInput) {
        Vector<ContentValues> cVValues = new Vector<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonInput);
            String movieId = jsonObject.getString("id");
            JSONArray records = jsonObject.getJSONArray("results");
            for (int i=0; i<records.length(); i++) {
                JSONObject record = (JSONObject)records.get(i);

                String id = record.getString("id");
                String author = record.getString("author");
                String content = record.getString("content");

                ContentValues val = new ContentValues();
                val.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);
                val.put(MoviesContract.ReviewEntry.COLUMN_REVIEW_ID, id);
                val.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, author);
                val.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, content);
                cVValues.add(val);
            }
            Log.d(LOG_TAG, "Got " + records.length() + " records!");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to interpret response", e);
        }
        return cVValues;
    }

    @Override
    protected Uri getUri() {
        return MoviesContract.ReviewEntry.CONTENT_URI;
    }


}
