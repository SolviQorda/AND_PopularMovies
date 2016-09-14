package qorda_projects.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sorengoard on 12/09/16.
 */
public class TrailerAdapter extends ArrayAdapter<MovieTrailer>{

    final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    public TrailerAdapter(Context context, ArrayList<MovieTrailer> movieTrailers) {
        super(context, 0, movieTrailers);
    }
    static class ViewHolder {
        public TextView trailerName;
        public Button trailerWatch;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieTrailer trailer = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_list_item_detail, parent, false);

            holder = new ViewHolder();
            holder.trailerName = (TextView) convertView.findViewById(R.id.trailer_list_name);
            holder.trailerWatch = (Button) convertView.findViewById(R.id.trailer_list_watch);

            convertView.setTag(holder);

            if(trailer!=null) {
                if(holder.trailerName!= null) {
                    holder.trailerName.setText(trailer.getTrailerName());
                }
                if(holder.trailerWatch!= null) {
                    holder.trailerWatch.setContentDescription(trailer.getTrailerKey());
                }
            }
        }
        return convertView;
    }
}