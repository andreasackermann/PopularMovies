package de.andreasackermann.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import de.andreasackermann.popularmovies.MainActivity;

/**
 * Created by Andreas on 08.01.2017.
 */

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = MainActivity.class.getPackage().getName();

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final String PATH_TRAILERS = "trailers";

    public static final String PATH_REVIEWS = "reviews";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_ORIGINAL_TITLE = "title";

        /*
         * BLOB. The value is a blob of data, stored exactly as it was input.
         */
        public static final String COLUMN_IMAGE = "image";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_VOTE_AVG = "vote";

        public static final String COLUMN_POPULARITY = "popularity";

        public static final String COLUMN_RELEASED = "released";

        /*
         * https://www.sqlite.org/datatype3.html
         * SQLite does not have a separate Boolean storage class. Instead, Boolean values are stored as integers 0 (false) and 1 (true).
         * TODO maybe add check constraints
         */
        public static final String COLUMN_CAT_TOP_RATED = "is_top_rated";

        public static final String COLUMN_CAT_POPULAR = "is_popular";

        public static final String COLUMN_CAT_FAVORITE = "is_favorite";
    }


    public static final class ReviewEntry implements  BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static final String TABLE_NAME = "reviews";

        // foreign key to movies table
        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_REVIEW_ID = "review_id";

        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_CONTENT = "content";

    }


    public static final class TrailerEntry implements  BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        public static final String TABLE_NAME = "trailers";

        // foreign key to movies table
        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_TRAILER_ID = "trailer_id";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_SITE = "site";

        public static final String COLUMN_KEY = "key";


    }

    public static String getMovieIdFromUri(Uri uri) {
        return uri.getPathSegments().get(1);
    }

}
