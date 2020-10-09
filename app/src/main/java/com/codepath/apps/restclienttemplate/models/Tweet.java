package com.codepath.apps.restclienttemplate.models;

import android.provider.ContactsContract;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/*
The user tweet model, which is used to create the tweet.
@Entity used for creating a database of recent tweets for offline mode
 */
@Entity(foreignKeys = @ForeignKey
        (entity = User.class,
                parentColumns = "id",
                childColumns = "userId"))//Defines foreign key, including class relation
@Parcel
public class Tweet {

    @ColumnInfo
    public String body;

    @ColumnInfo
    public String createdAt;

    @PrimaryKey
    @ColumnInfo
    public long id;

    @ColumnInfo
    public long userId;

    @Ignore
    public User user;

    //Empty constructor required for Parceler
    public Tweet () {}

    /*
    Creates the tweet based on JSON parts
     */
    public static Tweet fromJson (JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.id = jsonObject.getLong("id");

        //User is an object, so create similar Tweet fromJson method to retrieve
        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.userId = user.id;
        return tweet;
    }

    /*
    Creates the array of tweets we want to retrieve
     */
    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List <Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {//Adds
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
