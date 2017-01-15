package de.andreasackermann.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.andreasackermann.popularmovies.MoviesAdapter;

/**
 * Created by Andreas on 12.01.2017.
 */

public class MoviesDbHelper extends SQLiteOpenHelper {

    private final String LOG_TAG = MoviesDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 6;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " + MoviesContract.MovieEntry.TABLE_NAME +
                        " (" + MoviesContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                        MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                        MoviesContract.MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                        MoviesContract.MovieEntry.COLUMN_RELEASED + " TEXT NOT NULL, " +
                        MoviesContract.MovieEntry.COLUMN_IMAGE + " TEXT NOT NULL, " +
                        MoviesContract.MovieEntry.COLUMN_FAVORITE_IDX + " INTEGER, " +
                        MoviesContract.MovieEntry.COLUMN_VOTE_AVG + " REAL, " +
                        MoviesContract.MovieEntry.COLUMN_POPULARITY + " REAL);";

        Log.d(LOG_TAG, "SQL_CREATE_MOVIES_TABLE=" + SQL_CREATE_MOVIES_TABLE);

        db.execSQL(SQL_CREATE_MOVIES_TABLE);

        final String SQL_CREATE_REVIEWS_TABLE =
                "CREATE TABLE " + MoviesContract.ReviewEntry.TABLE_NAME +
                        " (" + MoviesContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoviesContract.ReviewEntry.COLUMN_REVIEW_ID + " TEXT UNIQUE ON CONFLICT REPLACE, " +
                        MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " INTEGER, " +
                        MoviesContract.ReviewEntry.COLUMN_AUTHOR + " TEXT, " +
                        MoviesContract.ReviewEntry.COLUMN_CONTENT + " TEXT, " +
                        " FOREIGN KEY (" + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MoviesContract.MovieEntry.TABLE_NAME + " (" + MoviesContract.MovieEntry._ID + "))";

        Log.d(LOG_TAG, "SQL_CREATE_REVIEWS_TABLE=" + SQL_CREATE_REVIEWS_TABLE);

        db.execSQL(SQL_CREATE_REVIEWS_TABLE);

        final String SQL_CREATE_TRAILERS_TABLE =
                "CREATE TABLE " + MoviesContract.TrailerEntry.TABLE_NAME +
                        " (" + MoviesContract.TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoviesContract.TrailerEntry.COLUMN_TRAILER_ID + " TEXT UNIQUE ON CONFLICT REPLACE, " +
                        MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " INTEGER, " +
                        MoviesContract.TrailerEntry.COLUMN_KEY + " TEXT, " +
                        MoviesContract.TrailerEntry.COLUMN_NAME + " TEXT, " +
                        MoviesContract.TrailerEntry.COLUMN_SITE + " TEXT, " +
                        " FOREIGN KEY (" + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MoviesContract.MovieEntry.TABLE_NAME + " (" + MoviesContract.MovieEntry._ID + "))";

        Log.d(LOG_TAG, "SQL_CREATE_TRAILERS_TABLE=" + SQL_CREATE_TRAILERS_TABLE);

        db.execSQL(SQL_CREATE_TRAILERS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.TrailerEntry.TABLE_NAME);
        onCreate(db);
    }
}


