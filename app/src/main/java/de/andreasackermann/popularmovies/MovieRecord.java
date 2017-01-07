package de.andreasackermann.popularmovies;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andreas on 02.12.2016.
 */

public class MovieRecord implements Parcelable {

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getMoviePosterImageThumbnail() {
        return moviePosterImageThumbnail;
    }

    public void setMoviePosterImageThumbnail(String moviePosterImageThumbnail) {
        this.moviePosterImageThumbnail = moviePosterImageThumbnail;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public MovieRecord(String originalTitle, String moviePosterImageThumbnail, String overview, String voteAverage, String releaseDate) {
        this.setOriginalTitle(originalTitle);
        this.setMoviePosterImageThumbnail(moviePosterImageThumbnail);
        this.setOverview(overview);
        this.setVoteAverage(voteAverage);
        this.setReleaseDate(releaseDate);
    }

    private String originalTitle;
    private String moviePosterImageThumbnail;
    private String overview;
    private String voteAverage;
    private String releaseDate;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalTitle);
        dest.writeString(moviePosterImageThumbnail);
        dest.writeString(overview);
        dest.writeString(voteAverage);
        dest.writeString(releaseDate);
    }

    private MovieRecord(Parcel in) {
        originalTitle = in.readString();
        moviePosterImageThumbnail = in.readString();
        overview = in.readString();
        voteAverage = in.readString();
        releaseDate = in.readString();
    }

    public static final Parcelable.Creator<MovieRecord> CREATOR
            = new Parcelable.Creator<MovieRecord>() {
        public MovieRecord createFromParcel(Parcel in) {
            return new MovieRecord(in);
        }

        public MovieRecord[] newArray(int size) {
            return new MovieRecord[size];
        }
    };

    public static class Parser {

        private final static String LOG_TAG= Parser.class.getSimpleName();

        public static ArrayList<MovieRecord> parse(String jsonInput) {

            ArrayList<MovieRecord> moviesList = new ArrayList<>();
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
                    moviesList.add(new MovieRecord(
                            title,
                            builder.toString(),
                            overview,
                            voteAverage,
                            releaseDate
                    ));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to interpret response", e);
            }
            return moviesList;
        }

    }

}
