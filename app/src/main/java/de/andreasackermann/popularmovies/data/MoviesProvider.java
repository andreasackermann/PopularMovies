package de.andreasackermann.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Movie;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Andreas on 08.01.2017.
 */

public class MoviesProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private final String LOG_TAG = MoviesProvider.class.getSimpleName();

    private static final int MOVIES = 100;

    private static final int MOVIE_DETAIL = 101;

    private static final int TRAILERS = 200;

    private static final int TRAILERS_FOR_MOVIE = 201;

    private static final int REVIEWS = 300;

    private static final int REVIEWS_FOR_MOVIE = 301;

    private MoviesDbHelper mDbHelper;

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
            case REVIEWS_FOR_MOVIE:
                sqLiteQueryBuilder.setTables(MoviesContract.ReviewEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = " + MoviesContract.getMovieIdFromUri(uri));
                break;
            case TRAILERS:
                sqLiteQueryBuilder.setTables(MoviesContract.TrailerEntry.TABLE_NAME);
                break;
            case TRAILERS_FOR_MOVIE:
                sqLiteQueryBuilder.setTables(MoviesContract.TrailerEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " = " + MoviesContract.getMovieIdFromUri(uri));
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Cursor cursor = sqLiteQueryBuilder.query(mDbHelper.getReadableDatabase(), projection,selection,selectionArgs,null,null,sortOrder);
        Log.d(LOG_TAG, "Returning " + cursor.getCount() + " hits for uri " + uri);
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
                // keep personal favorite setting
                long _id = insertOrUpdateById(db, uri, MoviesContract.MovieEntry.TABLE_NAME, values);
                returnUri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, _id);
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

    /**
     * Idea based on http://stackoverflow.com/questions/23417476/use-insert-or-replace-in-contentprovider
     * In case of a conflict when inserting the values, another update query is sent.
     *
     * @param db     Database to insert to.
     * @param uri    Content provider uri.
     * @param table  Table to insert to.
     * @param values The values to insert to.
     * @throws android.database.SQLException
     */
    private long insertOrUpdateById(SQLiteDatabase db, Uri uri, String table,
                                    ContentValues values) throws SQLException {
            int nrRows = update(uri, values, MoviesContract.MovieEntry._ID + "=?",
                    new String[]{values.getAsString(MoviesContract.MovieEntry._ID)});
            if (nrRows > 0)
                return Long.parseLong(values.getAsString(MoviesContract.MovieEntry._ID));
            return db.insertOrThrow(table, null, values);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int insertCount = 0;
        String tableName;
        db.beginTransaction();
        try {
            switch (match) {
                case MOVIES:
                    for (ContentValues value : values) {
                        long _id = insertOrUpdateById(
                                db,
                                uri,
                                MoviesContract.MovieEntry.TABLE_NAME,
                                value);
                        if (_id != -1) {
                            insertCount++;
                        }
                    }
                    break;
                case REVIEWS:
                    tableName= MoviesContract.ReviewEntry.TABLE_NAME;
                    for (ContentValues value : values) {
                        long _id = db.insert(tableName, null, value);
                        if (_id != -1) {
                            insertCount++;
                        }
                    }
                    break;
                case TRAILERS:
                    tableName= MoviesContract.TrailerEntry.TABLE_NAME;
                    for (ContentValues value : values) {
                        long _id = db.insert(tableName, null, value);
                        if (_id != -1) {
                            insertCount++;
                        }
                    }
                    break;
                default:
                    return super.bulkInsert(uri, values);
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
            case MOVIE_DETAIL:
                if (selection==null) {
                    updateCount = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, MoviesContract.MovieEntry._ID + " = ?", new String[] { MoviesContract.getMovieIdFromUri(uri) });
                } else {
                    throw new UnsupportedOperationException("Update only allowed based on _ID");
                }
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
        Log.d(LOG_TAG, "Update count = " + updateCount);
        return updateCount;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIE_DETAIL);
        matcher.addURI(authority, MoviesContract.PATH_TRAILERS, TRAILERS);
        matcher.addURI(authority, MoviesContract.PATH_TRAILERS + "/#", TRAILERS_FOR_MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS + "/#", REVIEWS_FOR_MOVIE);
        return matcher;
    }
}
