package de.andreasackermann.popularmovies.json;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import de.andreasackermann.popularmovies.BuildConfig;
import de.andreasackermann.popularmovies.MovieRecord;

/**
 * Created by Andreas on 15.01.2017.
 */

public abstract class JsonHelper {

    private final static String LOG_TAG = JsonHelper.class.getName();

    private static final String PROTOCOL = "https";
    private static final String API_ENDPOINT = "api.themoviedb.org";
    private static final String API_VERSION = "3";
    private static final String API_BASE = "movie";

    protected static final String API_KEY = "api_key";

    protected Uri.Builder httpUriBuilder;

    private Context context;

    public JsonHelper(Context context) {
        httpUriBuilder = new Uri.Builder();
        httpUriBuilder.scheme(PROTOCOL);
        httpUriBuilder.authority(API_ENDPOINT);
        httpUriBuilder.appendPath(API_VERSION);
        httpUriBuilder.appendPath(API_BASE);

        this.context = context;
    }

    public void updateDb() {
        String resp = getResponse();
        Vector<ContentValues> val = parseInput(resp);
        insertToDb(val);
    }

    /**
     * solution from
     * http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
     * as suggested in implementation guide
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected String getResponse() {
        if (isOnline()) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {


                Uri uri = httpUriBuilder.build();

                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuffer sb = new StringBuffer();
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        sb.append(line);
                    }
                    return sb.toString();
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                    }
                }
            }
        }
        return null;
    }

    protected abstract Vector<ContentValues> parseInput(String jsonInput);

    protected abstract Uri getUri();

    protected void insertToDb(Vector<ContentValues> cVValues) {
        for (int i = 0; i<cVValues.size(); i++) {
            context.getContentResolver().insert(getUri(), cVValues.get(i));
        }
    }



}