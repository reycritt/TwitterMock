package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.util.List;

//extending class is second step (fix error to include override methods)
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{
    List<Tweet> tweets;
    Context context;

    //Pass in context and list of tweets (list created in tweet model); use generate
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.tweets = tweets;
        this.context = context;
    }

    //For each row, inflate layout for tweet
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);

        return new ViewHolder(view);//Refers to first step
    }

    //Bind values based on position of element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Get data at position
        Tweet tweet = tweets.get(position);
        //Bind tweet with viewholder
        holder.bind(tweet);//Call bind (does not exist until after typed), create in ViewHolder
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    /*
    Wipes all currently visible tweets from adapter to refresh
     */
    public void clear () {
        tweets.clear();//Makre sure to modify current, not make new
        notifyDataSetChanged();
    }

    /*
    Adds newly emptied tweets to replace old tweets with recent tweets
     */
    public void addAll (List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    //Define viewholder (first step)
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvCreatedAt;

        public ViewHolder(@NonNull View itemView) {//Represents a single row in recycle
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }

        /*
        This defines the values put onto the Views (Text, Image, etc)
         */
        public void bind (Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvCreatedAt.setText(tweet.createdAt);
            //Use glide for profile image
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
        }
    }
}
