package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    TwitterClient client;
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
                    //Avoids error by always clearing first
                    adapter.clear();
                    /*
                    Replaced original 2 commented lines with addAll() for refresh
                    and to make adapter easier to update tweets
                     */
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));

                    //Call setrefresh to signal finish, after refresh listener was called
                    swipeContainer.setRefreshing(false);
                    /*
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                     */
                    //Remember that notifying reloads the adapter with the new tweets
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