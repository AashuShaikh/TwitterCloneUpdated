package com.aashushaikh.twitterclone.data

data class Message(
    val sender: String = "",
    val receiver: String = "",
    val message: String = "",
    val timestamp: Long = 0
)
