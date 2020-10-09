package com.codepath.apps.restclienttemplate.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao//Data Access Object


public interface TweetDao {
    //Selects all from tweet.userId and useId, while displaying 5 of what is selected
    @Query("SELECT Tweet.body AS tweet_body, " +
            "Tweet.createdAt AS tweet_createdAt, " +
            "Tweet.id AS tweet_id, User.* FROM tweet " +
            "INNER JOIN User ON Tweet.userId = userId " +
            "ORDER BY Tweet.createdAt " +
            "DESC LIMIT 5")
    List<TweetWithUser> recentItems();//get result SQL

    @Insert(onConflict = OnConflictStrategy.REPLACE)//If conflicts, replace them
    void insertModel(Tweet... tweets);//"..." means it can take any number of tweets

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(User... users);
}
