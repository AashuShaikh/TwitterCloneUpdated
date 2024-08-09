package com.aashushaikh.twitterclone.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.aashushaikh.twitterclone.R
import com.aashushaikh.twitterclone.adapters.TweetAdapter
import com.aashushaikh.twitterclone.data.Tweet
import com.aashushaikh.twitterclone.databinding.FragmentTweetsBinding
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

class TweetsFragment : Fragment() {

    private lateinit var binding: FragmentTweetsBinding
    private lateinit var tweetAdapter: TweetAdapter
    private lateinit var tweets: MutableList<Tweet>
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tweets, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = Firebase.auth
        tweets = mutableListOf()
        tweetAdapter = TweetAdapter(tweets)
        binding.rvTweets.adapter = tweetAdapter
        binding.rvTweets.layoutManager = LinearLayoutManager(requireContext())

        binding.btnAddTweet.setOnClickListener {
            createDialog()
        }

        fetchTweets()
    }

    private fun createDialog() {
        val inflater = layoutInflater
        val customView = inflater.inflate(R.layout.add_tweet_layout, null)
        val btnAddTweet = customView.findViewById<MaterialButton>(R.id.btn_add_new_tweet)
        val etTitle = customView.findViewById<TextInputEditText>(R.id.et_tweet_title)
        val etBody = customView.findViewById<TextInputEditText>(R.id.et_tweet_body)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(customView)
            .setTitle("Add New Tweet")
            .create()

        dialog.show()

        btnAddTweet.setOnClickListener {
            if (etTitle.text.toString().isNotEmpty() && etBody.text.toString().isNotEmpty()) {
                val tweetTitle = etTitle.text.toString()
                val tweetBody = etBody.text.toString()
                val tweet = Tweet(
                    title = tweetTitle,
                    body = tweetBody,
                    user = mAuth.currentUser?.email.toString()
                )
                Firebase.database.getReference("users").child(mAuth.currentUser!!.uid)
                    .child("tweets").push().setValue(tweet)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Tweet added successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to add tweet", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill all the fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun addTweetToListOfTweets() {
        TODO()
    }

    private fun fetchTweets() {
        val userTweetsRef = Firebase.database.getReference("users").child(mAuth.currentUser!!.uid)
        userTweetsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tweets.clear()

                // Handle fetching of tweets
                if (snapshot.child("tweets").exists()) {
                    val tweetMap = snapshot.child("tweets").value as? Map<*, *>
                    val tweetList = tweetMap?.values?.mapNotNull { tweetMapItem ->
                        (tweetMapItem as? Map<*, *>).let {
                            it?.let { map -> Tweet(
                                title = map["title"] as? String ?: "",
                                body = map["body"] as? String ?: "",
                                user = map["user"] as? String ?: ""
                            ) }
                        }
                    }?.toMutableList()

                    if (tweetList != null) {
                        tweets.addAll(tweetList)
                    }
                }

                // Handle fetching of followings
                val listOfFollowings = snapshot.child("listOfFollowings").value as? MutableList<String>
                for (following in listOfFollowings!!) {
                    (following).let { followingId ->
                        Firebase.database.getReference("users").child(followingId).child("tweets")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (tweetSnapshot in snapshot.children) {
                                        val tweet = tweetSnapshot.getValue(Tweet::class.java)
                                        tweet?.let {
                                            tweets.add(it)
                                        }
                                    }
                                    tweetAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("TweetsFragment", "Failed to fetch tweets", error.toException())
                                }
                            })
                    }
                }

                tweetAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TweetsFragment", "Failed to fetch tweets", error.toException())
            }
        })
    }


}