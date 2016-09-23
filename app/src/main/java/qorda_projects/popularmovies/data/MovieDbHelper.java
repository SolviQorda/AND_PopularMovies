package qorda_projects.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sorengoard on 12/09/16.
 */
public class MovieDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + moviesContract.MoviesEntry.TABLE_NAME + " (" +
                moviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                moviesContract.MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                moviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                moviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                moviesContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                moviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, " +
                moviesContract.MoviesEntry.COLUMN_DB_ID + " TEXT NOT NULL, " +
                moviesContract.MoviesEntry.COLUMN_FAVOURITE + " INTEGER, " +
                moviesContract.MoviesEntry.COLUMN_VIDEOS + " TEXT, " +
                moviesContract.MoviesEntry.COLUMN_REVIEWS + " TEXT" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        //TODO: add commands to upgrade
        sqLiteDatabase.execSQL("CLOSE TABLE IF EXISTS" + moviesContract.MoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
