package qorda_projects.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by sorengoard on 12/09/16.
 */
public class moviesProvider extends ContentProvider{

    private static final String LOG_TAG = moviesProvider.class.getSimpleName();

    //uri matcher
    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private MovieDbHelper mDbHelper;

    //Integer constants for URI types --> how do we know what these need to be?
    static final int MOVIES = 100;
    static final int MOVIE_SINGLE = 101;
    static final int MOVIES_FAVOURITES = 200;


    private static String sMovieByIdSetting =
            moviesContract.MoviesEntry.TABLE_NAME + "." +
                    moviesContract.MoviesEntry.COLUMN_DB_ID + " = ? ";

    //is there we want to write select * from movies where favourite=0?
    private static String sMoviesByFavouriteSetting =
            moviesContract.MoviesEntry.TABLE_NAME + "." +
                    moviesContract.MoviesEntry.COLUMN_FAVOURITE + " = ? ";

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        String idSetting = moviesContract.MoviesEntry.getDbIdFrmUri(uri);

        return mDbHelper.getWritableDatabase().query(moviesContract.MoviesEntry.TABLE_NAME,
                projection,
                sMovieByIdSetting,
                new String[]{idSetting},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMoviesByFavourite(Uri uri, String[] projection, String sortOrder) {
        String favouriteSetting = moviesContract.MoviesEntry.getFavouriteStatusFromUri(uri);

        return mDbHelper.getWritableDatabase().query(moviesContract.MoviesEntry.TABLE_NAME,
                projection,
                sMoviesByFavouriteSetting,
                new String[]{favouriteSetting},
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher(){
        //TODO: fill this
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = moviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, moviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, moviesContract.PATH_MOVIES + "/0", MOVIES_FAVOURITES);
        matcher.addURI(authority, moviesContract.PATH_MOVIES + "/#", MOVIE_SINGLE);

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
        switch (match) {
            case(MOVIES):
            return moviesContract.MoviesEntry.CONTENT_TYPE;
            case(MOVIE_SINGLE):
            return moviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case(MOVIES_FAVOURITES):
            return moviesContract.MoviesEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        //For now default but if different cases add control flow.
        Cursor retCursor;
        switch(mUriMatcher.match(uri)) {
            case(MOVIES):
            retCursor = mDbHelper.getReadableDatabase().query(
                    moviesContract.MoviesEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
                break;
            case(MOVIE_SINGLE):
                //getMovieByid
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            case(MOVIES_FAVOURITES):
//                        retCursor = mDbHelper.getReadableDatabase().query(
//                                moviesContract.MoviesEntry.TABLE_NAME,
//                                projection,
//                                selection,
//                                selectionArgs,
//                                null,
//                                null,
//                                sortOrder
//
//                );
            retCursor = getMoviesByFavourite(uri, projection, sortOrder);
                    break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

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
        Log.d(LOG_TAG, "_id: " + Long.toString(_id));
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
        int rowsUpdated;
        final int match = mUriMatcher.match(uri);

        switch(match) {
            case(MOVIES):
                rowsUpdated = db.update(moviesContract.MoviesEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            case(MOVIE_SINGLE):
                rowsUpdated = db.update(moviesContract.MoviesEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
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

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //for use if more cases
        final int match = mUriMatcher.match(uri);

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {

                long _id = db.insert(moviesContract.MoviesEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;

        //return super.bulkInsert(uri, values);

    }



}
