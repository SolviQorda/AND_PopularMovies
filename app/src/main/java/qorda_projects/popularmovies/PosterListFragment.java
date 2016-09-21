package qorda_projects.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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


public class PosterListFragment extends Fragment {


    private static final String PLACEHOLDER = "PLACEHOLDER: TO REPLACE WITH DATA";
    private static final String LOG_TAG = PosterListFragment.class.getSimpleName();
    public static ImageAdapter mMoviesAdapter;
    public GridView posterGridView;
    public static ArrayList<MovieElement> mMovies;

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
        if(isOnline()) {

            updateMovies();
            return rootView;
        }
        else
        {
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
        //TODO: surely this needs to change, hardcoded to popularity
        String sortBy = prefs.getString(getString(R.string.pref_search_category_key), (getString(R.string.pref_search_category_popularity)));
        moviesTask.execute(sortBy);
     }

    public boolean isOnline()
    {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class fetchMoviesTask extends AsyncTask<String, Void, MovieElement[]> {

        //TODO: does it make sense to not use a movieelement object when we have a database?

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
//            Log.v(LOG_TAG, "results array : "+ resultsStrArray.toString());

            // TODO: insert this data into db -- how could we get the SQL id out?
            // Don't actually need the vector unless doing a bulk insert.
            Vector<ContentValues> cVVector = new Vector<ContentValues>(resultsStrArray.length);

            for(int i = 0;i<resultsStrArray.length;i++){
                String posterPath = resultsStrArray[i].getPosterUrl();
                String synopsis = resultsStrArray[i].getSynopsis();
                String title = resultsStrArray[i].getTitle();
                String userRating = resultsStrArray[i].getUserRating();
                String releaseDate = resultsStrArray[i].getReleaseDate();
                // mdb_id to differentiate from db table id.
                String mdbId = resultsStrArray[i].getMovieId();

                ContentValues movieValues = new ContentValues();

                movieValues.put(moviesContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_OVERVIEW, synopsis);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_TITLE, title);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, userRating);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_DB_ID, mdbId);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_FAVOURITE, 0);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_VIDEOS, PLACEHOLDER);
                movieValues.put(moviesContract.MoviesEntry.COLUMN_REVIEWS, PLACEHOLDER);

                getContext().getContentResolver().insert(moviesContract.MoviesEntry.CONTENT_URI, movieValues);
                cVVector.add(movieValues);


            }
            Log.d(LOG_TAG, "insert:" + cVVector.size() + " inserted");
            //


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

//                Log.v(LOG_TAG, "Built URI: " + builtUri.toString());

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
//                Log.v(LOG_TAG, "JSON string: "+ movieJsonStr);

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
//            Log.v(LOG_TAG, "Movie array: " + result.toString());
            if(result != null){
                mMovies.clear();
                //loop to pass result into an arrayList of MovieElements
                for(int i = 0; i < result.length;i++)
                {
                    mMovies.add(result[i]);
//                    Log.v(LOG_TAG, "Movie: " + result[i].toString());

                }
            }

            //hook up gridview to the adapter
            mMoviesAdapter = new ImageAdapter(getContext(), mMovies);

            posterGridView.setAdapter(mMoviesAdapter);

            //TODO: This now needs to pass a Uri bundle through an intent.

            posterGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                //trying to get movie_element object through the adapter.
                MovieElement movie =  mMovies.get(position);
                String mbd_id = movie.getMovieId();

                //TODO: pass the mbd_id to build a uri based on db id.
                //TODO: the only way of id'ing the movie is through its position and its details inside the
                //TODO: movie object. So the URI builder needs to find the id and return the corresponding row
                //TODO: If not ID, then how do we access the _ID of the row and bundle that?

                //build a query that effectively gets * for a row where column_db_id == mbdid

                Bundle movieArgs = new Bundle();
                        Uri dbIdUri = moviesContract.MoviesEntry.buildMovieWithDbId(
                                mbd_id);
                movieArgs.putParcelable("dbIdUri", dbIdUri);
                Log.v(LOG_TAG, "mdb_id uri: " + dbIdUri.toString());
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class).
                        putExtras(movieArgs);
                PosterListFragment.this.startActivity(detailIntent);
            }

        });

        }


    }




}
