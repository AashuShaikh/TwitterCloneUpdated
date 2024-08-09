package com.aashushaikh.twitterclone.data

data class User(
    val uid: String = "",
    val email: String = "",
    val profileImage: String = "",
    val listOfFollowings: List<String> = listOf(),
    val listOfTweets: List<String> = listOf()
)
