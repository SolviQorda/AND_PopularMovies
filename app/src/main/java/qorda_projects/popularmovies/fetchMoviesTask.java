package qorda_projects.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by sorengoard on 28/09/16.
 */
public class fetchMoviesTask extends AsyncTask<String, Void, MovieElement[]> {

    private static final String LOG_TAG = PosterListFragment.class.getSimpleName();
    public static ArrayList<MovieElement> mMovies;
    public Context mContext;
    public AsyncCallback mAsyncCallback;

    public fetchMoviesTask(Context context,AsyncCallback asyncCallback){
        mContext = context;
        mAsyncCallback = asyncCallback;
    }

    public interface AsyncCallback {
        void updateData(ArrayList<MovieElement> mMovies);
    }

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
        mMovies = new ArrayList<MovieElement>(Arrays.asList(resultsStrArray));

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

        String MDBapiKey = BuildConfig.API_KEY;

        try {
            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String MDB_API_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendEncodedPath(params[0])
                    .appendQueryParameter(MDB_API_PARAM, MDBapiKey)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "url: " + url);

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
        mAsyncCallback.updateData(mMovies);
        if(PosterListFragment.mMoviesAdapter!=null) {
            PosterListFragment.mMoviesAdapter.notifyDataSetChanged();
        }
////        mMovies = new ArrayList<MovieElement>();
////        if (result != null) {
////            mMovies.clear();
////            //loop to pass result into an arrayList of MovieElements
////            for (int i = 0; i < result.length; i++) {
////                mMovies.add(result[i]);
////            }
////        }
    }

}

