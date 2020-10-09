package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {
    public static final int MAX_TWEET_LENGTH = 140;
    public static final String TAG = "ComposeActivity";
    EditText etCompose;
    Button btnTweet;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        //Similar to TimelineActivity
        client = TwitterApp.getRestClient(this);

        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);

        //Set click listener on button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetConent = etCompose.getText().toString();
                if (tweetConent.isEmpty()) {
                    //If tweet/edit text is empty; think about Android Snackbar for warnings, instead of Toast
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tweetConent.length() > MAX_TWEET_LENGTH) {
                    //If tweet/edit text is longer than 140 characters
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(ComposeActivity.this, tweetConent, Toast.LENGTH_SHORT).show();
                //Make API call to Twitter to publish tweet
                client.publishTweet(tweetConent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet: ");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says: " + tweet.body);

                            //ComposeActivity does not close on its own; using parent activity,
                            //return a result for the parent to use
                            Intent intent = new Intent();

                            /*
                            We must use Parseler to parse more complex data between intents
                            intent.putExtra("tweet", tweet);
                             */
                            intent.putExtra("tweet", Parcels.wrap(tweet));

                            //Set result code and bundle data for response
                            setResult(RESULT_OK, intent);
                            finish();//Closes current activity

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet: ", throwable);
                    }
                });
            }
        });

    }
}