package com.aashushaikh.twitterclone.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utilities {
    fun getFormattedTime(time: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }
}