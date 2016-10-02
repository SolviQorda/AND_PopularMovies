package qorda_projects.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by sorengoard on 08/08/16.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<MovieElement> mMovies;
    private static final String LOG_TAG = PosterListFragment.class.getSimpleName();


    public ImageAdapter(Context context, ArrayList<MovieElement> MovieElements) {
        mContext = context;
        mMovies = MovieElements;
    }


    public int getCount() {
        if(mMovies==null){
            return 0;
        } else {
            return mMovies.size();
        }
    }

    public void determineMoviePaths(ArrayList<MovieElement> paths) {
        mMovies = paths;
    }

    public Object getItem(int position) {
        //need to use this to access a movie object.
        return position;
    }

    public Object getMovieItem(int position, MovieElement[] results)
    {
        return results[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    // Create a new imageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        MovieElement movie = mMovies.get(position);
        if (convertView == null) {
            //if its not recycled, initialise some attributes
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(0, 0, 0, 0);

        } else {
            imageView = (ImageView) convertView;
        }
        //Using Picasso to load into imageView

        final String URLroot = "http://image.tmdb.org/t/p/w342/";

                String imageUrl = movie.getPosterUrl();

                Picasso.with(mContext).
                        load(URLroot + imageUrl).
                        into(imageView);

        return imageView;
    }

}
