package com.aashushaikh.twitterclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.aashushaikh.twitterclone.data.User
import com.aashushaikh.twitterclone.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = Firebase.auth
        Log.d("TAGG", "Current User: "+mAuth.currentUser?.email.toString())

        if(mAuth.currentUser != null){
            val signInIntent = Intent(this, HomeActivity::class.java)
            startActivity(signInIntent)
            finish()
        }

        clickHandler()
    }

    private fun clickHandler() {
        binding.btnLogin.setOnClickListener {
            if(binding.etEmail.text != null && binding.etPassword.text != null){
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                login(email, password)
            }else{
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSignup.setOnClickListener {
            if(binding.etEmail.text != null && binding.etPassword.text != null){
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                signUp(email, password)
            }else{
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val signInIntent = Intent(this, HomeActivity::class.java)
                    startActivity(signInIntent)
                    finish()
                } else {
                    Toast.makeText(
                        this, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.etEmail.text = null
                    binding.etPassword.text = null
                }
            }
    }

    private fun addUserToDB(user: User){
        Firebase.database.getReference("users").child(user.uid).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Logged in Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d("TAGG", "Error adding document", e)
            }
    }

    private fun signUp(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = User(
                        mAuth.uid.toString(),
                        email,
                        "",
                        listOf(""),
                        listOf("")
                    )
                    addUserToDB(user)
                } else {
                    Toast.makeText(
                        this, "Failed to Sign Up!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}