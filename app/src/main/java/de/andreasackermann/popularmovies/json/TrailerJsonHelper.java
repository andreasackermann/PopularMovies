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
 * Created by Andreas on 17.01.2017.
 */

public class TrailerJsonHelper extends JsonHelper {

    private final static String LOG_TAG = TrailerJsonHelper.class.getName();

    private final static String PATH_VIDEOS = "videos";

    public TrailerJsonHelper(Context context, String movieId) {
        super(context);
        httpUriBuilder.appendPath(movieId);
        httpUriBuilder.appendPath(PATH_VIDEOS);
        httpUriBuilder.appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY);
    }


    @Override
    protected Vector<ContentValues> parseInput(String jsonInput) {
        Vector<ContentValues> cVValues = new Vector<ContentValues>();
        try {
            JSONObject jsonObject = new JSONObject(jsonInput);
            String movieId = jsonObject.getString("id");
            JSONArray records = jsonObject.getJSONArray("results");
            for (int i=0; i<records.length(); i++) {
                JSONObject record = (JSONObject)records.get(i);

                String id = record.getString("id");
                String key = record.getString("key");
                String name = record.getString("name");
                String site = record.getString("site");

                ContentValues val = new ContentValues();
                val.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_ID, id);
                val.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID, movieId);
                val.put(MoviesContract.TrailerEntry.COLUMN_KEY, key);
                val.put(MoviesContract.TrailerEntry.COLUMN_NAME, name);
                val.put(MoviesContract.TrailerEntry.COLUMN_SITE, site);

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
        return MoviesContract.TrailerEntry.CONTENT_URI;
    }

}
