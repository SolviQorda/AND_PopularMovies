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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import java.util.ArrayList;

import qorda_projects.popularmovies.data.moviesContract;


public class PosterListFragment extends Fragment implements LoaderManager.LoaderCallbacks<MovieElement>, fetchMoviesTask.AsyncCallback {

    private static final String LOG_TAG = PosterListFragment.class.getSimpleName();

    public static ImageAdapter mMoviesAdapter;
    private int mPosition = GridView.INVALID_POSITION;
    public fetchMoviesTask.Status asyncStatus;
    public String mSortBy;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
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
        public void onItemSelected(Uri movieUri);
    }

    @Override
    public void updateData(ArrayList<MovieElement> mMovies){
        this.mMovies = mMovies;
        mMoviesAdapter.notifyDataSetChanged();
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                updateMovies();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);


        //hook up gridview to the adapter

        isTablet = ((MainActivity) getActivity()).isTablet();
        Log.v(LOG_TAG, "savedInstance: " + savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(getResources().getString(R.string.selected_key))){
            mPosition = savedInstanceState.getInt(getResources().getString(R.string.selected_key));

            mMovies = savedInstanceState.getParcelableArrayList("mMovies");
            Log.v(LOG_TAG, "MMovies: " + mMovies);
            mMoviesAdapter = new ImageAdapter(getContext(), mMovies);

            posterGridView.setAdapter(mMoviesAdapter);
            }
        else {
            if (savedInstanceState == null && isOnline()) {

                updateMovies();
                mSortBy = prefs.getString(getString(R.string.pref_search_category_key), (getString(R.string.pref_search_category_default)));
                pushToDatabase(mMovies);

                } else {
                    Toast.makeText(getContext(), getString(R.string.no_network_error), Toast.LENGTH_LONG).show();
                }


                posterGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    //trying to get movie_element object through the adapter.
                    mPosition = position;
                    MovieElement movie = mMovies.get(position);
                    String mbd_id = movie.getMovieId();
                    Bundle movieArgs = new Bundle();
                    Uri dbIdUri = moviesContract.MoviesEntry.buildMovieWithDbId(
                            mbd_id);
                    movieArgs.putParcelable("dbIdUri", dbIdUri);
                    ((Callback) getActivity()).onItemSelected(dbIdUri);
                    if (isTablet) {
                        DetailFragment fragment = new DetailFragment();
                        fragment.setArguments(movieArgs);
                    }

                }

            });

            if (mPosition != GridView.INVALID_POSITION) {
                posterGridView.smoothScrollToPosition(mPosition);
            }
            Bundle movieList = new Bundle();

            movieList.putParcelableArrayList("mMovies", mMovies);
            mMoviesAdapter = new ImageAdapter(getContext(), mMovies);

            movieList.putInt(getResources().getString(R.string.selected_key), mPosition);
            onSaveInstanceState(movieList);

        }
        return rootView;

    }

    public void onStart()
    {super.onStart();}


    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String resumeSortBy = prefs.getString(getString(R.string.pref_search_category_key), (getString(R.string.pref_search_category_default)));
        //update movies
        if (!resumeSortBy.equals(mSortBy)){
            updateMovies();
            mMoviesAdapter.notifyDataSetChanged();
        }
        //set adapter

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
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
    public void updateMovies() {
        fetchMoviesTask moviesTask = new fetchMoviesTask(this.getContext(), this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = prefs.getString(getString(R.string.pref_search_category_key), (getString(R.string.pref_search_category_default)));

        Log.v(LOG_TAG, "SortBy: "+ sortBy);
        if(sortBy.equals("favourites")){
            favouritesTask();
        } else {
            moviesTask.execute(sortBy);
            mMovies = fetchMoviesTask.mMovies;
            Log.v(LOG_TAG, "mmovies:" + mMovies);
            mMoviesAdapter = new ImageAdapter(getContext(), mMovies);
            posterGridView.setAdapter(mMoviesAdapter);

            if(moviesTask.getStatus() == fetchMoviesTask.Status.RUNNING){
                asyncStatus = fetchMoviesTask.Status.RUNNING;
            } else if (moviesTask.getStatus() == fetchMoviesTask.Status.FINISHED){
                asyncStatus = fetchMoviesTask.Status.FINISHED;
            }

        }
     }

    public void pushToDatabase(ArrayList<MovieElement> mMovies) {

        for (int i = 0; i < mMovies.size(); i++) {
            String posterPath = mMovies.get(i).getPosterUrl();
            String synopsis = mMovies.get(i).getSynopsis();
            String title = mMovies.get(i).getTitle();
            String userRating = mMovies.get(i).getUserRating();
            String releaseDate = mMovies.get(i).getReleaseDate();
            String mdbId = mMovies.get(i).getMovieId();

            ContentValues movieValues = new ContentValues();

            movieValues.put(moviesContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
            movieValues.put(moviesContract.MoviesEntry.COLUMN_OVERVIEW, synopsis);
            movieValues.put(moviesContract.MoviesEntry.COLUMN_TITLE, title);
            movieValues.put(moviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, userRating);
            movieValues.put(moviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
            movieValues.put(moviesContract.MoviesEntry.COLUMN_DB_ID, mdbId);
            movieValues.put(moviesContract.MoviesEntry.COLUMN_FAVOURITE, 1);
            movieValues.put(moviesContract.MoviesEntry.COLUMN_VIDEOS, getResources().getString(R.string.placeholder));
            movieValues.put(moviesContract.MoviesEntry.COLUMN_REVIEWS, getResources().getString(R.string.placeholder));


            getContext().getContentResolver().insert(moviesContract.MoviesEntry.CONTENT_URI, movieValues);
        }
    }

    public boolean isOnline()
    {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(getResources().getString(R.string.selected_key), mPosition);
        }
        outState.putParcelableArrayList("mMovies", mMovies);
        outState.putInt(getResources().getString(R.string.selected_key), mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            updateMovies();
        }
    }

    //Cursor methods
    @Override
    public Loader<MovieElement> onCreateLoader(int id, Bundle args) {
        return null;
   }

    @Override
    public void onLoadFinished(Loader<MovieElement> loader, MovieElement data) {
        return;
    }

    @Override
    public void onLoaderReset(Loader<MovieElement> loader) {
        return;
    }

    @TargetApi(16)
    protected void favouritesTask() {
        //this needs to query the database and populate the view and then support an onClick
        Uri favouriteUri = moviesContract.MoviesEntry.buildFavouriteMoviesUriWithFavouriteStatus(
                getResources().getString(R.string.favourite_query)
        );
        Cursor favouritesCursor = getContext().getContentResolver().query(
                favouriteUri,
                MOVIE_COLUMNS,
                null,
                null,
                null
        );
        int favouritesLength = favouritesCursor.getCount();
        Log.v(LOG_TAG, "faveLength: " + favouritesLength);
        if (favouritesLength >= 1) {
            favouritesCursor.moveToFirst();
            //
            mMovies = new ArrayList<MovieElement>();
            if (mMovies != null) {
                mMovies.clear();
            }
            while (favouritesCursor.moveToNext()) {

                String db_id = favouritesCursor.getString(COL_MOVIE_DB_ID);
                String title = favouritesCursor.getString(COL_MOVIE_TITLE);
                String synopsis = favouritesCursor.getString(COL_MOVIE_OVERVIEW);
                String userRating = favouritesCursor.getString(COL_MOVIE_VOTE);
                String releaseDate = favouritesCursor.getString(COL_MOVIE_RELEASE);
                String posterPath = favouritesCursor.getString(COL_MOVIE_POSTER_PATH);
                Log.v(LOG_TAG, "favePoster: " + posterPath);
                int favouriteStatus = favouritesCursor.getInt(COL_MOVIE_FAVOURITE);
                MovieElement favouriteMovie = new MovieElement();

                favouriteMovie.setTitle(title);
                favouriteMovie.setSynopsis(synopsis);
                favouriteMovie.setUserRating(userRating);
                favouriteMovie.setReleaseDate(releaseDate);
                favouriteMovie.setPosterUrl(posterPath);
                favouriteMovie.setFavouriteStatus(favouriteStatus);
                favouriteMovie.setMovieId(db_id);
                mMovies.add(favouriteMovie);
                    if(mMovies!=null){
                        for(int q=favouritesCursor.getPosition()+1;q<mMovies.size();q++) {
                            String existingMovieId = favouriteMovie.getMovieId();
                            if (db_id.equals(existingMovieId)) {
                                mMovies.remove(q);
                                break;
                            }
                        }
                        favouritesCursor.moveToNext();


                    }
            }
        }
        if(PosterListFragment.mMoviesAdapter!=null) {
            PosterListFragment.mMoviesAdapter.notifyDataSetChanged();
        }
    }

}
