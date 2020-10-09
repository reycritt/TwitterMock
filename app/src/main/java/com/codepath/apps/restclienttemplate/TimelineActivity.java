package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    private final int REQUEST_CODE = 20;
    TwitterClient client;
    TweetDao tweetDao;
    RecyclerView rvTweets;//First steps for recyclerview/adapter
    List<Tweet> tweets;
    TweetsAdapter adapter;
    /*
    Swipe to refresh, added in timeline activity xml
    (because this is where tweets show up)
    */
    SwipeRefreshLayout swipeContainer;

    //Endless recyle
    EndlessRecyclerViewScrollListener scrollListener;

    private static final String TAG = "TimelineActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();


        //Swipe to refresh
        swipeContainer = findViewById(R.id.swipeContainer);
        //Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "Fetching new data");
                populateHomeTimeline();
            }
        });

        //Find recycler view
        rvTweets = findViewById(R.id.rvTweets);
        //Initialize list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        //Redefined layoutmanager for use in infinity scroll
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //Configure recyclerview - layout manager, adapter
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore: " + page);
                loadMoreData();//Use Android to generate this method
            }
        };

        //Adds scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        //Query for existing tweets in DB; placed in new thread to avoid crashing
        //due to overworking Android
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Showing data from db");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });

        populateHomeTimeline();
    }

    private void loadMoreData() {
        //1. Send API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for loadMoreData" + json.toString());
                //2. Deserialaize and construct new model objects from API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    //3. Append new data objects to existing set of items inside array of items
                    Tweet.fromJsonArray(jsonArray);
                    //4. Notify adapter of new items made with "notifyItemRangeInserted()"
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure for loadMoreTweets", throwable);
            }
        }, tweets.get(tweets.size() - 1).id);

    }

    /*
    Inflates menu_main
    Note that ctrl + O opens override menu showing all override functions available!!!!
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu; adds items to action bar if present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;//In documentation, this override method MUST return true
    }

    /*
    Can override this method, or use "onClick" on menu item in xml for app bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose) {
            //Compose (app bar item/button) icon has been tapped
            //Toast.makeText(this, "Composed!", Toast.LENGTH_SHORT).show();
            //Navigate to compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    Used for the parent activity (activity which starts an intent) to obtain a result
    after the completion of a child activity (the activity started)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Confirms the successful return of request and result
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            /*
            Get data from intent (the composed tweet) using getExtra from ComposedActivity
            represented by Intent data variable; make note that the tweet is a Tweet model,
            created by the user, and is initialized here
             */
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            //Update RV with new tweet
            //Modify data source of tweets
            tweets.add(0, tweet);//Adds new tweet on top of current timeline
            //Update adapter
            adapter.notifyItemInserted(0);
            //Forces the rv to scroll to specified position, in this case the top (0)
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
                The timeline consists of all of the tweets/posts recommended to user
                 */
    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess! " + json.toString());
                //Fetch actual tweets useing Tweet model
                JSONArray jsonArray = json.jsonArray;
                try {
                    //Common variable
                    final List<Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);

                    //Avoids error by always clearing first
                    adapter.clear();
                    /*
                    Replaced original 2 commented lines with addAll() for refresh
                    and to make adapter easier to update tweets
                     */
                    adapter.addAll(tweetsFromNetwork);

                    //Call setrefresh to signal finish, after refresh listener was called
                    swipeContainer.setRefreshing(false);
                    /*
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                     */
                    //Remember that notifying reloads the adapter with the new tweets

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "saving data into db");
                            //Insert users first (users must be populated first for foreign key)
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));
                            //Insert tweets next
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                //Attempting to retrieve data from endpoint, so we concat response to get full info
                Log.e(TAG, "onFailure! " + response, throwable);

            }
        });
    }
}