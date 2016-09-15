package qorda_projects.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by sorengoard on 10/08/16.
 */
public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    public final String DETAIL_URI = "URI";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().
                    add(R.id.movie_detail_container, new DetailFragment()).
                    commit();

            Bundle arguments = new Bundle();
            if (arguments != null) {
                arguments.putParcelable(DETAIL_URI, getIntent().getData());
            }
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction().add(R.id.movie_detail_container, detailFragment)
                    .commit();
        }


    }
}
