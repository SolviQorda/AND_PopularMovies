package qorda_projects.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by sorengoard on 12/09/16.
 */
public class moviesContract {

    //content authority
    public static final String CONTENT_AUTHORITY = "qorda_projects.projects.popularmovies";
    //base URI
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //possible paths
    public static final String PATH_MOVIES = "movies";

    //implement columns of table
    public static final class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        //TODO: go back to content type and content item type to double check, plus content resolver
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";
        // data returned from a standard popular request
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_DB_ID = "id";
        public static final String COLUMN_FAVOURITE = "favourite";

        // data returned from a videos request
        public static final String COLUMN_VIDEO_KEYS ="videos";
        //data returned from a reviews request
        public static final String COLUMN_REVIEWS = "reviews";

        public static Uri buildMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
