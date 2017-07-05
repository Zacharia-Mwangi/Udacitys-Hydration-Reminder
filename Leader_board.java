package com.example.zack.hydrationreminder;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zack.hydrationreminder.Network_Utilities.NetworkUtils;
import com.example.zack.hydrationreminder.*;
import com.example.zack.hydrationreminder.utilities.PreferenceUtilities;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;

public class Leader_board extends AppCompatActivity {

    private EditText mSearchBoxEditText;
    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private TextView mCount;
    private TextView mName;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        // go back to Visualizer activity on clicking back button
        ActionBar actionBar = this.getSupportActionBar();


        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        mSearchResultsTextView = (TextView) findViewById(R.id.tv_github_search_results_json);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mCount = (TextView) findViewById(R.id.textView5);
        mName = (TextView) findViewById(R.id.textView6);



        mCount.setText(PreferenceUtilities.getWaterCount(this)+"");
        mName.setText("Jane");

       



    }



    // check if home button is pressed, navigate Up
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        } else if (id == R.id.action_search){
                Context context = Leader_board.this;
                String message = "Search Clicked";
                Toast.makeText(context,message, Toast.LENGTH_LONG).show();

                makeGithubSearchQuery();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }


    public void makeGithubSearchQuery(){
        URL githubSearchUrl = NetworkUtils.buildURL();
        // mUrlDisplayTextView.setText(githubSearchUrl.toString());
        // Create a new GithubQueryTask and call its execute method, passing in the url to query
        new GithubQueryTask().execute(githubSearchUrl);
    }

    public class GithubQueryTask extends AsyncTask<URL, Void, String> {
        // set loading indicator visible
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params){
            URL searchUrl = params[0];
            String githubSearchResults = null;
            try{
                githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);

            } catch (IOException e){
                e.printStackTrace();
            }

            return  githubSearchResults;
        }

        // Override onPostExecute to display the results in the TextView
        @Override
        protected void onPostExecute(String githubSearchResults) {
            // on completion, hide indicator
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (githubSearchResults != null && !githubSearchResults.equals("")) {
                showJsonDataView();
                try {
                    JSONObject json= (JSONObject) new JSONTokener(githubSearchResults).nextValue();
                    JSONObject json2 = json.getJSONObject("results");
                    String test = (String) json2.get("name");
                    mSearchResultsTextView.setText(test);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // mSearchResultsTextView.setText(githubSearchResults);
            } else {
                // show error message
                showErrorMessage();
            }
        }
    }

    // show the data and hide the error
    private void showJsonDataView(){
        // Make error invisible
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        //Show JSON data
        mSearchResultsTextView.setVisibility(View.VISIBLE);

    }

    // Show error, Hide data
    private void showErrorMessage(){
        //Hide current visible data
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        //Show Error
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }
}
