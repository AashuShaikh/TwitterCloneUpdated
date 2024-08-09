package com.aashushaikh.twitterclone

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.aashushaikh.twitterclone.databinding.ActivityMessagesBinding
import com.aashushaikh.twitterclone.databinding.FragmentFollowingsBinding

class MessagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessagesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_messages)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.message_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.profile -> {
//                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
//                true
//            }
//
//            R.id.voice_call -> {
//                Toast.makeText(this, "Voice Call", Toast.LENGTH_SHORT).show()
//                true
//            }
//
//            R.id.video_call -> {
//                Toast.makeText(this, "Video Call", Toast.LENGTH_SHORT).show()
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}