package com.aashushaikh.twitterclone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.aashushaikh.twitterclone.adapters.ViewPagerAdapter
import com.aashushaikh.twitterclone.databinding.ActivityHomeBinding
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var selectedImage: Uri
    private lateinit var profileImage: CircleImageView
    private lateinit var btnAddProfile: MaterialButton
    private var storageRef: StorageReference = Firebase.storage.getReference("Images")

    val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImage = result.data?.data!!
                profileImage.setImageURI(selectedImage)
                btnAddProfile.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.appToolbar)

        mAuth = Firebase.auth

        setupViewPager()

    }

    private fun setupViewPager() {
        binding.viewPager.adapter = ViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Accounts"
                    tab.setIcon(R.drawable.ic_profile)
                }

                1 -> {
                    tab.text = "Tweets"
                    tab.setIcon(R.drawable.ic_tweets)
                }
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.twitter_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                createDialog()
                true
            }

            R.id.logout -> {
                mAuth.signOut()
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }

            R.id.message -> {
                startActivity(Intent(this, MessagesActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createDialog() {
        val inflater = layoutInflater
        val customView = inflater.inflate(R.layout.add_profile_layout, null)
        btnAddProfile = customView.findViewById(R.id.btn_add_profile)
        profileImage = customView.findViewById(R.id.profile_image)
        Log.d("TAGG", "createDialog: ${profileImage}")
        Firebase.database.getReference("users").child(mAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val link = snapshot.child("profileImage").value.toString()
                    if(link.isNotEmpty()){
                        Glide.with(this@HomeActivity).load(link).into(profileImage)
                    }else{
                        profileImage.setImageResource(R.drawable.ic_add_photo)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
                }

            })
        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setTitle("Add Profile Picture")
            .create()

        dialog.show()

        profileImage.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryResultLauncher.launch(galleryIntent)
        }

        btnAddProfile.setOnClickListener {
            if (profileImage.id != R.drawable.ic_add_photo) {
                val fileName = UUID.randomUUID().toString() + ".jpg"
                val storage = storageRef.child(fileName)
                storage.putFile(selectedImage).addOnSuccessListener {
                    val result = it.metadata?.reference?.downloadUrl
                    result?.addOnSuccessListener { uri ->
                        Firebase.database.getReference("users").child(mAuth.currentUser!!.uid)
                            .child("profileImage").setValue(uri.toString())
                        Toast.makeText(this, "Profile Picture Added", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            } else {
                Toast.makeText(
                    this,
                    "You can add Profile picture anytime later",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }

    }


}