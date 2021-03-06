package qorda_projects.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import qorda_projects.popularmovies.data.moviesContract;

/**
 * Created by sorengoard on 15/09/16.
 */

//TODO: refactor fragment into another class and refactor fetchtrailersandreviews into another class

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public final String LOG_TAG = DetailFragment.class.getSimpleName();
    public String mdbId;
    private Uri mUri;
    private int favourite;


    public static ArrayList<MovieTrailer> mTrailers;
    public static ArrayList<MovieReview> mReviews;

    //Writing projection

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            moviesContract.MoviesEntry.TABLE_NAME + "." + moviesContract.MoviesEntry._ID,
            moviesContract.MoviesEntry.COLUMN_OVERVIEW,
            moviesContract.MoviesEntry.COLUMN_TITLE,
            moviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            moviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            moviesContract.MoviesEntry.COLUMN_DB_ID,
            moviesContract.MoviesEntry.COLUMN_FAVOURITE,
            moviesContract.MoviesEntry.COLUMN_POSTER_PATH

    };
    private static int COL_MOVIE_TABLE_ID = 0;
    private static int COL_MOVIE_OVERVIEW = 1;
    private static int COL_MOVIE_TITLE = 2;
    private static int COL_MOVIE_VOTE = 3;
    private static int COL_MOVIE_RELEASE = 4;
    private static int COL_MOVIE_DB_ID = 5;
    private static int COL_MOVIE_FAVOURITE = 6;
    private static int COL_MOVIE_POSTER_PATH = 7;

    private ListView reviewListView;
    private ListView trailerListView;
    private ImageView mPosterImageView;
    private TextView mTitleView;
    private TextView mReleaseView;
    private TextView mRatingView;
    private Button mFavouriteButton;
    private TextView mSynopsisView;
    public Button watchButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if(arguments != null){
            mUri = arguments.getParcelable("URI");
            if (mUri != null) {
                String idStr = mUri.toString();
                mdbId = idStr.substring(55);
            }
        }

        View rootView = inflater.inflate(R.layout.activity_detail_fragment, container, false);
        trailerListView = (ListView) rootView.findViewById(R.id.listView_trailers);
        reviewListView = (ListView) rootView.findViewById(R.id.listView_reviews);
        watchButton = (Button) rootView.findViewById(R.id.trailer_list_watch);
        mPosterImageView = (ImageView) rootView.findViewById(R.id.thumbnail);
        mSynopsisView = (TextView) rootView.findViewById(R.id.detail_synopsis);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title);
        mRatingView = (TextView) rootView.findViewById(R.id.detail_userRating);
        mReleaseView = (TextView) rootView.findViewById(R.id.detail_releaseDate);
        mFavouriteButton = (Button) rootView.findViewById(R.id.detail_favourites);

        //SPLASH
        mFavouriteButton.setBackgroundColor(getResources().getColor(R.color.colorTitle));
        mReleaseView.setText(getResources().getText(R.string.splash_instructions));


                MovieElement movie = new MovieElement();

            if(mdbId != null) {
                fetchTrailersAndReviews fetchTrailersAndReviewsTask = new fetchTrailersAndReviews();
                fetchTrailersAndReviewsTask.execute(movie);

            }
        Log.v(LOG_TAG, "mdbId:" + mdbId);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if(null != mUri) {

            return new CursorLoader(getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.moveToFirst()) {
            mFavouriteButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            String synopsis = data.getString(COL_MOVIE_OVERVIEW);
            String posterUrl = data.getString(COL_MOVIE_POSTER_PATH);
            int movieDbId = data.getInt(COL_MOVIE_DB_ID);
            favourite = data.getInt(COL_MOVIE_FAVOURITE);
            String favouriteStr;
            String tableId = data.getString(COL_MOVIE_TABLE_ID);
            String title = data.getString(COL_MOVIE_TITLE);
            String userRating = data.getString(COL_MOVIE_VOTE) + "/10";
            String releaseDate = data.getString(COL_MOVIE_RELEASE).substring(0, 4);
            Log.v(LOG_TAG, "faveStat of" + title + "is" + Integer.toString(favourite));
            if(favourite == 0){
                favouriteStr = getResources().getString(R.string.remove_favourites);
            } else {
                favouriteStr = getResources().getString(R.string.add_favourites);
            }

            mSynopsisView.setText(synopsis);
            mTitleView.setText(title);
            mRatingView.setText(userRating);
            mReleaseView.setText(releaseDate);
            mFavouriteButton.setText(favouriteStr);

            final String URLroot = "http://image.tmdb.org/t/p/w185/" + posterUrl;
            Uri imageUri = Uri.parse(URLroot).buildUpon().build();

            mPosterImageView.setImageURI(imageUri);
            Picasso.with(getContext()).
                    load(imageUri).
                    into(mPosterImageView);

            mFavouriteButton.setOnClickListener(new Button.OnClickListener(){

                @Override
                public void onClick(View view) {


                    ContentValues favouriteValue = new ContentValues();
                    String mSelectionClause = moviesContract.MoviesEntry.COLUMN_DB_ID + " = ?";
                    String[] selectionArgs = {mdbId};

                    if (favourite == 1) {
                        favouriteValue.put(moviesContract.MoviesEntry.COLUMN_FAVOURITE, favourite - 1);
                        getContext().getContentResolver().update(mUri, favouriteValue, mSelectionClause, selectionArgs);
                        mFavouriteButton.setText(getResources().getString(R.string.remove_favourites));
                        Toast.makeText(getContext(), getResources().getString(R.string.add_toast), Toast.LENGTH_SHORT).show();
                        favourite = 0;
                        favouriteValue.clear();
                    }
                     else if (favourite == 0){
                        favouriteValue.put(moviesContract.MoviesEntry.COLUMN_FAVOURITE, favourite + 1);
                        getContext().getContentResolver().update(mUri, favouriteValue, mSelectionClause, selectionArgs);
                        mFavouriteButton.setText(getResources().getString(R.string.add_favourites));
                        Toast.makeText(getContext(), getResources().getString(R.string.remove_toast), Toast.LENGTH_SHORT).show();
                        favourite = 1;
                        favouriteValue.clear();
                    }
                }

            });

        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorloader) { }

    //New async task here for movies and reviews - needs to take id from resultsStrArray

    public class fetchTrailersAndReviews extends AsyncTask<MovieElement, Void, MovieElement> {

        final String MDB_RESULTS = "results";
        final String MDB_REVIEW_AUTHOR = "author";
        final String MDB_REVIEW_CONTENT = "content";
        final String MDB_TRAILER_KEY = "key";
        final String MDB_TRAILER_NAME = "name";

        public MovieElement trailersAndReviews = new MovieElement();

        private MovieReview[] getReviewDataFromJson(String reviewJsonById)
                throws JSONException {
            JSONObject reviewListJson = new JSONObject(reviewJsonById);

            JSONArray reviewResultsArray = reviewListJson.getJSONArray(MDB_RESULTS);
            int length = reviewResultsArray.length();
            MovieReview[] movieReviewsResults = new MovieReview[length];
            //for loop to collect each review.
            for (int i = 0;i < reviewResultsArray.length(); i++) {
                JSONObject reviewJson = reviewResultsArray.getJSONObject(i);
                MovieReview review = new MovieReview();

                String author = reviewJson.getString(MDB_REVIEW_AUTHOR);
                String content = reviewJson.getString(MDB_REVIEW_CONTENT);

                review.setAuthor(author);
                review.setContent(content);

                movieReviewsResults[i] = review;
            }

            trailersAndReviews.setMovieReviews(movieReviewsResults);
            return movieReviewsResults;
        }

        private MovieTrailer[] getTrailersDataFromJson(String movieById)
                throws JSONException{
            JSONObject trailerListJson = new JSONObject(movieById);
            JSONArray trailerResultsArray = trailerListJson.getJSONArray(MDB_RESULTS);
            int length = trailerResultsArray.length();
            MovieTrailer[] movieTrailersResults = new MovieTrailer[length];

            for(int i = 0;i < trailerResultsArray.length();i++) {
                JSONObject trailerJson = trailerResultsArray.getJSONObject(i);
                MovieTrailer trailer = new MovieTrailer();
                String key = trailerJson.getString(MDB_TRAILER_KEY);
                String name = trailerJson.getString(MDB_TRAILER_NAME);

                trailer.setTrailerKey(key);
                trailer.setTrailerName(name);

                movieTrailersResults[i] = trailer;

            }

            trailersAndReviews.setMovie_trailers(movieTrailersResults);
            return movieTrailersResults;
        }

        @Override
        protected MovieElement doInBackground(MovieElement... params) {
            MovieElement trailersAndReviews = new MovieElement();
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewJsonStr = null;
            String trailerJsonStr = null;

            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie";

            final String MDB_VIDEOS = "videos";
            final String MDB_REVIEWS = "reviews";
            final String MDB_API_PARAM = "api_key";

            String MDBapiKey = BuildConfig.API_KEY;

            //we only need the id
                Log.v(LOG_TAG, "movie id: " + mdbId);

                try {
                    Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                            .appendEncodedPath(mdbId)
                            .appendPath(MDB_REVIEWS)
                            .appendQueryParameter(MDB_API_PARAM, MDBapiKey).build();

                    URL url = new URL(builtUri.toString());

                    Log.v(LOG_TAG, "reviews URI: " + builtUri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    reviewJsonStr = buffer.toString();
                    Log.v(LOG_TAG, "Review JSON string: " + reviewJsonStr);

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error", e);

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error in closing stream", e);
                        }try {
                            getReviewDataFromJson(reviewJsonStr);
//                                Log.v(LOG_TAG, "reviews:" + reviews.toString());
                        } catch (JSONException e)
                        {
                            Log.e(LOG_TAG, e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }


                }


                //Now for trailers

                try {
                    Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                            .appendEncodedPath(mdbId)
                            .appendPath(MDB_VIDEOS)
                            .appendQueryParameter(MDB_API_PARAM, MDBapiKey).build();

                    URL url = new URL(builtUri.toString());

                    Log.v(LOG_TAG, "videos URI: " + builtUri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    trailerJsonStr = buffer.toString();
                    Log.v(LOG_TAG, "trailer JSON string: " + trailerJsonStr);

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error", e);

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error in closing stream", e);
                        }try {
                            getTrailersDataFromJson(trailerJsonStr);

                        } catch (JSONException e)
                        {
                            Log.e(LOG_TAG, e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }
                }

            Log.v(LOG_TAG, "t&v:" + trailersAndReviews.toString());
            return null;
        }


        @Override
        protected void onPostExecute(MovieElement result) {
            super.onPostExecute(result);

            if (trailersAndReviews!= null) {
                MovieTrailer[] trailersArray = trailersAndReviews.getMovieTrailers();
                MovieReview[] reviewsArray = trailersAndReviews.getMovieReviews();
                if (trailersArray != null) {
                    mTrailers = new ArrayList<MovieTrailer>(Arrays.asList(trailersArray));
                }
                if(reviewsArray != null){
                mReviews = new ArrayList<MovieReview>(Arrays.asList(reviewsArray));
                }
            }
            if (mTrailers != null) {
                TrailerAdapter trailerArrayAdapter = new TrailerAdapter(getContext(), mTrailers);
                trailerListView.setAdapter(trailerArrayAdapter);

            }
            //Need an onClick function with button to go to the correct video. Implicit intent
            //How do i make sure that this is linked to the button and not just the list item??

            if(mReviews != null) {
                ReviewAdapter reviewArrayAdapter = new ReviewAdapter(getContext(), mReviews);
                reviewListView.setAdapter(reviewArrayAdapter);
            }
        }


    }


}

