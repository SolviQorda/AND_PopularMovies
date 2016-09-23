package qorda_projects.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements PosterListFragment.Callback {

    private static final String LOG_TAG = PosterListFragment.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.movie_detail_container) != null) {

            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_detail_container, new PosterListFragment())
                        .commit();
            } else {
                mTwoPane = false;
            }

            PosterListFragment posterListFragment = ((PosterListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.container_main));

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        int id = menuItem.getItemId();
        if(id == R.id.action_settings)
        {
            //TODO: What receives this intent?
            //TODO: Read over notes on settings
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if(mTwoPane) {
            Bundle args = new Bundle();
            //TODO: how do we make sure that the parcelable puts the right uri?
            args.putParcelable("URI", contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
