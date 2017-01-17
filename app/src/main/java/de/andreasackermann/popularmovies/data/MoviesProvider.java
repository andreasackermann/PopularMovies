package de.andreasackermann.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Andreas on 08.01.2017.
 */

public class MoviesProvider extends ContentProvider {

    private final String LOG_TAG = MoviesProvider.class.getSimpleName();

    static final int MOVIES = 100;

    static final int MOVIE_DETAIL = 101;

    static final int TRAILERS = 200;

    static final int TRAILERS_FOR_MOVIE = 201;

    static final int REVIEWS = 300;

    static final int REVIEWS_FOR_MOVIE = 301;

    private static final String sTrailerByMovie =
            MoviesContract.TrailerEntry.TABLE_NAME + "." + MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?";

    private static final String sReviewByMovie =
            MoviesContract.ReviewEntry.TABLE_NAME + "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?";



    private MoviesDbHelper mDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();


    @Override
    public boolean onCreate() {
        mDbHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                sqLiteQueryBuilder.setTables(MoviesContract.MovieEntry.TABLE_NAME);
                break;
            case MOVIE_DETAIL:
                sqLiteQueryBuilder.setTables(MoviesContract.MovieEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MoviesContract.MovieEntry._ID + " = " +  MoviesContract.getMovieIdFromUri(uri));
                break;
            case REVIEWS:
                sqLiteQueryBuilder.setTables(MoviesContract.ReviewEntry.TABLE_NAME);
                break;
            case TRAILERS:
                sqLiteQueryBuilder.setTables(MoviesContract.TrailerEntry.TABLE_NAME);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Cursor cursor = sqLiteQueryBuilder.query(mDbHelper.getReadableDatabase(), projection,selection,selectionArgs,null,null,sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_DETAIL:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILERS:
                return MoviesContract.TrailerEntry.CONTENT_TYPE;
            case TRAILERS_FOR_MOVIE:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case REVIEWS:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case REVIEWS_FOR_MOVIE:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {

                long _id = db.insertWithOnConflict(MoviesContract.MovieEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {

                long _id = db.insertWithOnConflict(MoviesContract.ReviewEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = ContentUris.withAppendedId(MoviesContract.ReviewEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILERS: {

                long _id = db.insertWithOnConflict(MoviesContract.TrailerEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = ContentUris.withAppendedId(MoviesContract.TrailerEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(LOG_TAG, "Inserted: " + returnUri.toString());
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int insertCount = 0;
        String tableName;
        switch (match) {
            case MOVIES:
                tableName= MoviesContract.MovieEntry.TABLE_NAME;
                break;
            case REVIEWS:
                tableName= MoviesContract.ReviewEntry.TABLE_NAME;
                break;
            case TRAILERS:
                tableName= MoviesContract.TrailerEntry.TABLE_NAME;
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    insertCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int deleteCount;
        switch (match) {
            case MOVIES:
                deleteCount = db.delete(MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                deleteCount = db.delete(MoviesContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILERS:
                deleteCount = db.delete(MoviesContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (deleteCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int updateCount;
        switch (match) {
            case MOVIES:
                updateCount = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEWS:
                updateCount = db.update(MoviesContract.ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRAILERS:
                updateCount = db.update(MoviesContract.TrailerEntry.TABLE_NAME,values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (updateCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIE_DETAIL);
        matcher.addURI(authority, MoviesContract.PATH_TRAILERS, TRAILERS); //todo  + "/#"
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS); //todo  + "/#"
        return matcher;
    }

    private Cursor getTrailers(Uri uri) {
        return mDbHelper.getReadableDatabase().query(
                MoviesContract.TrailerEntry.TABLE_NAME,
                null,
                sTrailerByMovie,
                new String[] { MoviesContract.getMovieIdFromUri(uri) },
                null,
                null,
                null
                );
    }

    private Cursor getReviews(Uri uri) {
        return mDbHelper.getReadableDatabase().query(
                MoviesContract.ReviewEntry.TABLE_NAME,
                null,
                sReviewByMovie,
                new String[] { MoviesContract.getMovieIdFromUri(uri) },
                null,
                null,
                null
        );
    }

    private Cursor getMovie(Uri uri) {
        return mDbHelper.getReadableDatabase().query(
                MoviesContract.MovieEntry.TABLE_NAME,
                null,
                sReviewByMovie,
                new String[] { MoviesContract.getMovieIdFromUri(uri) },
                null,
                null,
                null
        );
    }

    private Cursor getMovies() {
        return mDbHelper.getReadableDatabase().query(
                MoviesContract.MovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
