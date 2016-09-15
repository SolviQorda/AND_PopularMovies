package qorda_projects.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by sorengoard on 12/09/16.
 */
public class ReviewAdapter  extends ArrayAdapter<MovieReview>{

    public ReviewAdapter(Context context, ArrayList<MovieReview> movieReviews) {
        super(context, 0, movieReviews);
    }

    static class ViewHolder {
        public TextView reviewAuthor;
        public TextView reviewContent;
    }

    @Override
    public View getView(int position,View convertView, ViewGroup parent) {
        MovieReview review = getItem(position);
        ViewHolder holder;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_list_item_detail, parent, false);

            holder = new ViewHolder();
            holder.reviewAuthor = (TextView) convertView.findViewById(R.id.review_list_author);
            holder.reviewContent = (TextView) convertView.findViewById(R.id.review_list_content);

            convertView.setTag(holder);

            if (review != null){
                if(holder.reviewAuthor != null){
                    holder.reviewAuthor.setText(review.getAuthor());
                }
                if(holder.reviewContent != null){
                    holder.reviewContent.setText(review.getContent());
                }
            }
        }

        return convertView;

    }
}
