package qorda_projects.popularmovies;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Vector;

import qorda_projects.popularmovies.data.moviesContract;


public class PosterListFragment extends Fragment{

    private static final String PLACEHOLDER = "PLACEHOLDER: TO REPLACE WITH DATA";
    private static final String LOG_TAG = PosterListFragment.class.getSimpleName();
    private static final String SELECTED_KEY = "selected_position";
    public static ImageAdapter mMoviesAdapter;
    private int mPosition = ListView.INVALID_POSITION;
    public GridView posterGridView;
    public static ArrayList<MovieElement> mMovies;
    public boolean isTablet;

    private static final String[] MOVIE_COLUMNS = {
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


    public PosterListFragment() {
        // Required empty public constructor
    }

    public interface Callback {
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_poster_list, container, false);
        posterGridView = (GridView) rootView.findViewById(R.id.gridview_posters);

        isTablet = ((MainActivity) getActivity()).isTablet();

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        if(isOnline()) {

            updateMovies();
            return rootView;
        }
        else
        {
            //TODO: get data from db
            Toast.makeText(getContext(), getString(R.string.no_network_error), Toast.LENGTH_LONG).show();
            return rootView;
        }

    }

    public void onStart()
    {

        super.onStart();

        //TODO: replace this with a sync adapter
        if(isOnline()) {

            updateMovies();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        menuInflater.inflate(R.menu.poster_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        int id = menuItem.getItemId();
        if(id == R.id.action_settings)
        {

            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            this.startActivity(settingsIntent);
            return true;
        }
        if(id == R.id.action_refresh)
        {
            updateMovies();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }
    private void updateMovies() {
        fetchMoviesTask moviesTask = new fetchMoviesTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = prefs.getString(getString(R.string.pref_search_category_key), (getString(R.string.pref_search_category_popularity)));

        Log.v(LOG_TAG, "SortBy: "+ sortBy);
        if(sortBy.equals("favourites")){
            favouritesTask();
        } else {
            moviesTask.execute(sortBy);
        }
     }

    public boolean isOnline()
    {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class fetchMoviesTask extends AsyncTask<String, Void, MovieElement[]> {

        //TODO: does it make sense to not use a movieelement object when we have a database?
        //NEED SOME KIND OF CHECK THAT THERE ISN'T A DATABASE

        private MovieElement[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {
            final String MDB_RESULTS = "results";
            final String MDB_POSTER = "poster_path";
            final String MDB_SYNOPSIS = "overview";
            final String MDB_TITLE = "title";
            final String MDB_USERRATTING = "vote_average";
            final String MDBRELEASE_DATE = "release_date";
            final String MDB_ID = "id";

            JSONObject movieJSON = new JSONObject(movieJsonStr);
            JSONArray JSONresultsArray = movieJSON.getJSONArray(MDB_RESULTS);

            MovieElement[] resultsStrArray= new MovieElement[JSONresultsArray.length()];

            for(int i = 0; i < JSONresultsArray.length();i++)
            {
                //get each object from the JSONarray and assign it as a variable
                JSONObject movie = JSONresultsArray.getJSONObject(i);

                String title = movie.getString(MDB_TITLE);
                String synopsis = movie.getString(MDB_SYNOPSIS);
                String releaseDate = movie.getString(MDBRELEASE_DATE);
                String userRating = movie.getString(MDB_USERRATTING);
                String posterUrl = movie.getString(MDB_POSTER);
                String movieId = movie.getString(MDB_ID);

                MovieElement MovieElement= new MovieElement();
                MovieElement.setTitle(title);
                MovieElement.setSynopsis(synopsis);
                MovieElement.setReleaseDate(releaseDate);
                MovieElement.setUserRating(userRating);
                MovieElement.setPosterUrl(posterUrl);
                MovieElement.setMovieId(movieId);

                        resultsStrArray[i] = MovieElement;
            }

            // TODO: insert this data into db -- how could we get the SQL id out?
            // Don't actually need the vector unless doing a bulk insert.
            Vector<ContentValues> cVVector = new Vector<ContentValues>(resultsStrArray.length);

            for(int i = 0;i<resultsStrArray.length;i++){
                String posterPath = resultsStrArray[i].getPosterUrl();
                String synopsis = resultsStrArray[i].getSynopsis();
                String title = resultsStrArray[i].getTitle();
                String userRating = resultsStrArray[i].getUserRating();
                String releaseDate = resultsStrArray[i].getReleaseDate();
                String mdbId = resultsStrArray[i].getMovieId();

                ContentValues movieValues = new ContentValues();

                movieValues.put(moviesContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_OVERVIEW, synopsis);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_TITLE, title);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, userRating);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_DB_ID, mdbId);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_FAVOURITE, 1);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_VIDEOS, PLACEHOLDER);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_REVIEWS, PLACEHOLDER);

                getContext().getContentResolver().insert(moviesContract.MoviesEntry.CONTENT_URI, movieValues);
                cVVector.add(movieValues);

            }

            return resultsStrArray;
        }


        @Override
        protected MovieElement[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;
            //TODO: Insert an API key here
            String MDBapiKey = "";

            try {
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String MDB_API_PARAM = "api_key";
                final String SORTBY_PARAM = "sort_by";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(MDB_API_PARAM, MDBapiKey)
                        .appendQueryParameter(SORTBY_PARAM, params[0] + ".desc").build();

                URL url = new URL(builtUri.toString());


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
                movieJsonStr = buffer.toString();

            } catch(IOException e)
            {
                Log.e(LOG_TAG, "Error", e);

            } finally {
                if(urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if(reader != null)
                {
                    try {
                        reader.close();
                    } catch(final IOException e)
                    {
                        Log.e(LOG_TAG, "Error in closing stream", e);
                    }
                }
            } try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(MovieElement[] result) {
            super.onPostExecute(result);
            mMovies = new ArrayList<MovieElement>();
            if(result != null){
                mMovies.clear();
                //loop to pass result into an arrayList of MovieElements
                for(int i = 0; i < result.length;i++)
                {
                    mMovies.add(result[i]);

                }
            }
            //hook up gridview to the adapter
            mMoviesAdapter = new ImageAdapter(getContext(), mMovies);

            posterGridView.setAdapter(mMoviesAdapter);

            //TODO: This now needs to pass a Uri bundle through an intent.
            //TODO: DO we really need two very similar methods for favourites and popular?

            posterGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
                {
                    //trying to get movie_element object through the adapter.
                    MovieElement movie =  mMovies.get(position);
                String mbd_id = movie.getMovieId();
                    Bundle movieArgs = new Bundle();
                    Uri dbIdUri = moviesContract.MoviesEntry.buildMovieWithDbId(
                            mbd_id);
                    movieArgs.putParcelable("dbIdUri", dbIdUri);

                    if (!isTablet) {

                        Intent detailIntent = new Intent(getActivity(), DetailActivity.class).
                                putExtras(movieArgs);
                        PosterListFragment.this.startActivity(detailIntent);
                    } else {
                        //if tablet
                        DetailFragment fragment = new DetailFragment();
                        fragment.setArguments(movieArgs);
                    }
            }

        });

        }


    }

    @TargetApi(16)
    protected void favouritesTask(){
        //this needs to query the database and populate the view and then support an onClick

        String favouriteWeWant = "0";
        Uri favouriteUri = moviesContract.MoviesEntry.buildFavouriteMoviesUriWithFavouriteStatus(favouriteWeWant);
        String sortOrder = ".desc";
        String[] selectionArgs = {favouriteWeWant};
        String mSelectionClause = moviesContract.MoviesEntry.COLUMN_FAVOURITE + " = ? ";
        Log.v(LOG_TAG, "selectionClause: " + mSelectionClause);
        Log.v(LOG_TAG, "faveUri: " + favouriteUri);
        Cursor favouritesCursor = getContext().getContentResolver().query(
                favouriteUri,
                MOVIE_COLUMNS,
                null,
                null,
                null
        );
        int favouritesLength = favouritesCursor.getCount();
        Log.v(LOG_TAG, "faveLength: " + favouritesLength);
        favouritesCursor.moveToFirst();

       final ArrayList<MovieElement> favouriteMovies = new ArrayList<MovieElement>();
        for(int i= 0;i < favouritesLength || i < 20; i++) {
            String title = favouritesCursor.getString(COL_MOVIE_TITLE);
            String synopsis = favouritesCursor.getString(COL_MOVIE_OVERVIEW);
            String userRating = favouritesCursor.getString(COL_MOVIE_VOTE);
            String releaseDate = favouritesCursor.getString(COL_MOVIE_RELEASE);
            String posterPath = favouritesCursor.getString(COL_MOVIE_POSTER_PATH);
            Log.v(LOG_TAG, "favePoster: " + posterPath);
            int favouriteStatus = favouritesCursor.getInt(COL_MOVIE_FAVOURITE);
            String db_id = favouritesCursor.getString(COL_MOVIE_DB_ID);

            MovieElement favouriteMovie = new MovieElement();

            favouriteMovie.setTitle(title);
            favouriteMovie.setSynopsis(synopsis);
            favouriteMovie.setUserRating(userRating);
            favouriteMovie.setReleaseDate(releaseDate);
            favouriteMovie.setPosterUrl(posterPath);
            favouriteMovie.setFavouriteStatus(favouriteStatus);
            favouriteMovie.setMovieId(db_id);
            favouriteMovies.add(favouriteMovie);
            favouritesCursor.moveToNext();
        }
        ImageAdapter faveMoviesAdapter = new ImageAdapter(getContext(), favouriteMovies);
        posterGridView.setAdapter(faveMoviesAdapter);

        posterGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                MovieElement movie = favouriteMovies.get(position);
                String mbd_id = movie.getMovieId();
                //if mobile
                Bundle movieArgs = new Bundle();
                Uri dbIdUri = moviesContract.MoviesEntry.buildMovieWithDbId(
                        mbd_id);
                movieArgs.putParcelable("dbIdUri", dbIdUri);

                if(!isTablet) {
                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class).
                            putExtras(movieArgs);
                    PosterListFragment.this.startActivity(detailIntent);
                }else {
                //if tablet
                    DetailFragment fragment = new DetailFragment();
                    fragment.setArguments(movieArgs);
                }
            }
        });
    }


}
