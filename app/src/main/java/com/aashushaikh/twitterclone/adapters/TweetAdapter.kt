package com.aashushaikh.twitterclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aashushaikh.twitterclone.R
import com.aashushaikh.twitterclone.data.Tweet

class TweetAdapter(private val tweets: List<Tweet>): RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    inner class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tweetTitle = itemView.findViewById<TextView>(R.id.tweet_title)
        val tweetBody = itemView.findViewById<TextView>(R.id.tweet_body)
        val userName = itemView.findViewById<TextView>(R.id.tweet_user)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TweetAdapter.TweetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_tweet, parent, false)
        return TweetViewHolder(view)
    }

    override fun onBindViewHolder(holder: TweetAdapter.TweetViewHolder, position: Int) {
        val currentTweet = tweets[position]
        holder.tweetTitle.text = currentTweet.title
        holder.tweetBody.text = currentTweet.body
        holder.userName.text = "~ " + currentTweet.user
    }

    override fun getItemCount(): Int {
        return tweets.size
    }
}