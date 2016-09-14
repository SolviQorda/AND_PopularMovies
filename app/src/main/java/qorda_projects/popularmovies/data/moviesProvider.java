package qorda_projects.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by sorengoard on 12/09/16.
 */
public class moviesProvider extends ContentProvider{

    //uri matcher
    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private MovieDbHelper mDbHelper;

    //Integer constants for URI types --> how do we know what these need to be?
    static final int MOVIES = 100;

    private static final SQLiteQueryBuilder sMoviesQuery;

    static{
        sMoviesQuery = new SQLiteQueryBuilder();
    }

    static UriMatcher buildUriMatcher(){
        //TODO: fill this
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = moviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, moviesContract.PATH_MOVIES, MOVIES);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = mUriMatcher.match(uri);

        //If different case, control flow them here.

        return moviesContract.MoviesEntry.CONTENT_TYPE;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //For now default but if different cases add control flow.
        Cursor retCursor = mDbHelper.getReadableDatabase().query(
                moviesContract.MoviesEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //for use if more cases
        final int match = mUriMatcher.match(uri);
        Uri returnUri;

        long _id = db.insert(moviesContract.MoviesEntry.TABLE_NAME, null, contentValues);
        if (_id > 0) {
            returnUri = moviesContract.MoviesEntry.buildMoviesUri(_id);
        } else {
            throw new android.database.SQLException("Failed to insert row into: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;


    }
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //for use if more cases
        final int match = mUriMatcher.match(uri);

        int rowsUpdated = db.update(moviesContract.MoviesEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated != 1) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //for use if more cases
        final int match = mUriMatcher.match(uri);

        if(selection == null) selection = "1";
        int rowsDeleted = db.delete(moviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }
}
