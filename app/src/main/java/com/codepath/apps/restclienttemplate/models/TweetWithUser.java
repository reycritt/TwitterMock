package com.codepath.apps.restclienttemplate.models;

import androidx.room.Embedded;

import java.util.ArrayList;
import java.util.List;

/*
Creates the SQL using all of the @ColumnInfo, etc
 */
public class TweetWithUser {

    //@Embedded flattens properties of User object into object, preserving encapsulation
    @Embedded
    User user;

    @Embedded(prefix = "tweet_")//Prefix all from Tweet table with "tweet_" to avoid confusion
    Tweet tweet;

    public static List<Tweet> getTweetList(List<TweetWithUser> tweetWithUsers) {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < tweetWithUsers.size(); i++) {
            //Explicitly setting User in each Tweet object, due to Room limitations
            Tweet tweet = tweetWithUsers.get(i).tweet;
            tweet.user = tweetWithUsers.get(i).user;
            tweets.add(tweet);
        }
        return tweets;
    }
}
