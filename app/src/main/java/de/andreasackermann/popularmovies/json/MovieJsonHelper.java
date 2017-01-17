package de.andreasackermann.popularmovies.json;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import de.andreasackermann.popularmovies.BuildConfig;
import de.andreasackermann.popularmovies.R;
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

    public MovieJsonHelper(Context context) {
        super(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String order = prefs.getString(context.getString(R.string.pref_order_key),context.getString(R.string.pref_order_default));
        httpUriBuilder.appendPath(order);
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
                String id = record.getString("id");
                String title = record.getString("title");
                String posterPath = record.getString("poster_path");
                File file = new File(context.getExternalFilesDir(null) + "/" + posterPath);
                try {


                    if (!file.exists()) {
                        // don't think we have to handle updates on the thumbnails
                        Bitmap bitmap;
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("http");
                        builder.authority("image.tmdb.org");
                        builder.appendPath("t");
                        builder.appendPath("p");
                        builder.appendPath("w185"); //size
                        builder.appendEncodedPath(posterPath);
                        Log.d(LOG_TAG, "Fetching File " + file.toString() + " from " + builder.build().toString());

                        bitmap = Picasso.with(context).load(builder.build()).get();

                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                        fos.flush();
                        fos.close();
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException");
                }

                String overview = record.getString("overview");
                String voteAverage = record.getString("vote_average");
                String releaseDate = record.getString("release_date");
                String popularity = record.getString("popularity");

                ContentValues val = new ContentValues();
                val.put(MoviesContract.MovieEntry._ID, id);
                val.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, title);
                val.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
                val.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, voteAverage);
                val.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, popularity);
                val.put(MoviesContract.MovieEntry.COLUMN_RELEASED, releaseDate);
                val.put(MoviesContract.MovieEntry.COLUMN_IMAGE, file.toString());

                cVValues.add(val);
            }
            Log.d(LOG_TAG, "Got " + records.length() + " records!");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to interpret response", e);
        }
        return cVValues;

    }
}
