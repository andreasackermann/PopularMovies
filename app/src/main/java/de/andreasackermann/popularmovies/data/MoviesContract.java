package de.andreasackermann.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

import de.andreasackermann.popularmovies.MainActivity;

/**
 * Created by Andreas on 08.01.2017.
 */

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = MainActivity.class.getPackage().getName();

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_ORIGINAL_TITLE = "title";

        /*
         * BLOB. The value is a blob of data, stored exactly as it was input.
         */
        public static final String COLUMN_IMAGE = "image";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_VOTE_AVG = "vote";

        public static final String COLUMN_RELEASED = "released";

        /*
         * https://www.sqlite.org/datatype3.html
         * SQLite does not have a separate Boolean storage class. Instead, Boolean values are stored as integers 0 (false) and 1 (true).
         * TODO maybe add check constraints
         */
        public static final String COLUMN_CAT_RATED = "rated";

        public static final String COLUMN_CAT_POPULAR = "popular";

        public static final String COLUMN_CAT_FAVORITE = "favorite";
    }

    public static final class ReviewEntry implements  BaseColumns {
        // foreign key to movies table
        public static final String COLUMN_MOVIE_ID = "movie_id";

    }

    public static final class TrailerEntry implements  BaseColumns {
        // foreign key to movies table
        public static final String COLUMN_MOVIE_ID = "movie_id";

    }
}
