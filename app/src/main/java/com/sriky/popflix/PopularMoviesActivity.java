package com.sriky.popflix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sriky.popflix.settings.SettingsActivity;
import com.sriky.popflix.utilities.MovieDataHelper;
import com.sriky.popflix.utilities.NetworkUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main Activity that is launched from the Launcher.
 * Responsible for querying TMDB's APIs and displaying movies in the specified order.
 */
public class PopularMoviesActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        PopularMoviesAdaptor.MoviePosterOnClickEventListener,
        LoaderManager.LoaderCallbacks<String>,
        FetchMovieDataTaskLoader.FetchMovieDataTaskListener {

    private static final String TAG = PopularMoviesActivity.class.getSimpleName();

    /*
     * Handle to the RecyclerView to aid in reset the list when user toggles btw
     * most_popular and top_rated movies from the settings menu.
     */
    @BindView(R.id.rv_posters)
    RecyclerView mMoviePostersRecyclerView;

    @BindView(R.id.pb_popularMoviesActivity)
    ProgressBar mProgressBar;

    @BindView(R.id.tv_error_msg)
    TextView mErrorMessageTextView;

    //handle to the adaptor instance.
    private PopularMoviesAdaptor mPopularMoviesAdaptor;

    //list to hold the downloaded movie data.
    private ArrayList<MovieData> mMovieDataArrayList;

    //query parameter for sorting ordering.
    private String mSortingOrder;

    //bool to keep track of change in preferences to display sort order for the movies.
    private boolean mDisplaySortingOrderChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);
        ButterKnife.bind(this);

        mMovieDataArrayList = new ArrayList<>();

        mMoviePostersRecyclerView.setHasFixedSize(true);

        mPopularMoviesAdaptor = new PopularMoviesAdaptor(getNumberOfItems(), this);
        mMoviePostersRecyclerView.setAdapter(mPopularMoviesAdaptor);

        showProgressBarAndHideErrorMessage();

        setSortingOrderFromSharedPreferences();

        //trigger the asynctaskloader to download movie data.
        //The following call will initialize a new loader if one doesn't exist.
        //If an old loader exist and has loaded the data, then onLoadFinished() will be triggered.
        getSupportLoaderManager().initLoader(MovieDataHelper.BASIC_MOVIE_DATA_LOADER_ID,
                getBundleForLoader(), PopularMoviesActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mDisplaySortingOrderChanged){
            mMovieDataArrayList.clear();
            getSupportLoaderManager().restartLoader(MovieDataHelper.BASIC_MOVIE_DATA_LOADER_ID,
                    getBundleForLoader(), PopularMoviesActivity.this);
            mDisplaySortingOrderChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.sort_order_key))) {
            mSortingOrder = sharedPreferences.getString(key, getString(R.string.default_sort_order));
            Log.d(TAG, "onSharedPreferenceChanged: mSortingOrder = " + mSortingOrder);
            mDisplaySortingOrderChanged = true;
        }
    }

    @Override
    public void onClickedItemAt(int index) {
        try {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDataHelper.MOVIE_ID_INTENT_EXTRA_KEY,
                    mMovieDataArrayList.get(index).getMovieID());
            startActivity(intent);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPreExecute() {
        Log.d(TAG, "onPreExecute: ()");
        showProgressBarAndHideErrorMessage();
    }


    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: id = "+id);
        return new FetchMovieDataTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null) {
            Log.d(TAG, "onLoadFinished: data.length() = " + data.length());
            mMovieDataArrayList.addAll(MovieDataHelper.getListfromJSONResponse(data));
            onDataLoadComplete();
        } else {
            onDataLoadFailed();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    /**
     * Generates a bundle with the query URL.
     * @return Bundle with query URL.
     */
    private Bundle getBundleForLoader() {
        Bundle bundleForLoader = new Bundle();
        bundleForLoader.putString(MovieDataHelper.FETCH_MOVIE_DATA_URL_KEY,
                NetworkUtils.buildURL(mSortingOrder, MovieDataHelper.TMDB_API_KEY).toString());

        return bundleForLoader;
    }

    /**
     * Sets the mSortingOrder variable to appropriate value from sharedPreference, if any.
     * Otherwise "popular" will be set as the default.
     */
    private void setSortingOrderFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSortingOrder = sharedPreferences.getString(getString(R.string.sort_order_key), getString(R.string.default_sort_order));
        Log.d(TAG, "setSortingOrderFromSharedPreferences: sortingOrder = " + mSortingOrder);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Displays the progress bar and hides the error message views.
     */
    private void showProgressBarAndHideErrorMessage() {
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the progress bar view and makes the the error message view VISIBLE.
     */
    private void hideProgressBarAndShowErrorMessage() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    /**
     * On successfully downloading data from TMDB
     * a new instance of PopularMoviesAdaptor is created based on the number of items
     * in the mMovieDataArrayList.
     * The new instance of PopularMoviesAdaptor is set to the mMoviePostersRecyclerView.
     */
    private void onDataLoadComplete() {
        Log.d(TAG, "onDataLoadComplete()");
        mProgressBar.setVisibility(View.INVISIBLE);//hide the progress bar.
        mPopularMoviesAdaptor.updateItemsCount(getNumberOfItems());
    }

    /**
     * If there were issues downloading data from TMDB, then hide the progress bar view and
     * display an error message to the user.
     */
    private void onDataLoadFailed() {
        Log.d(TAG, "onDataLoadFailed()");
        hideProgressBarAndShowErrorMessage();
    }

    /**
     * Gets the relative image path from TMDB for a specific index.
     *
     * @param index of the image.
     * @return relative path.
     * @throws ArrayIndexOutOfBoundsException when the index is out of bounds.
     */
    public String getImageRelativePathAtIndex(int index) throws ArrayIndexOutOfBoundsException {
        return mMovieDataArrayList.get(index).getPosterPath();
    }

    /**
     * Get the size of elements downloaded from TMDB.
     *
     * @return total number of the elements available to be displayed.
     */
    public int getNumberOfItems() {
        return mMovieDataArrayList.size();
    }
}
