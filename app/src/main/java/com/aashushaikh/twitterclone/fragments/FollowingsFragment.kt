package com.aashushaikh.twitterclone.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.aashushaikh.twitterclone.R
import com.aashushaikh.twitterclone.adapters.OnFollowClickListener
import com.aashushaikh.twitterclone.adapters.SuggestedAccountAdapter
import com.aashushaikh.twitterclone.data.SuggestedAccounts
import com.aashushaikh.twitterclone.databinding.FragmentFollowingsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FollowingsFragment : Fragment(), OnFollowClickListener {

    private lateinit var binding: FragmentFollowingsBinding
    private lateinit var followings: MutableList<SuggestedAccounts>
    private lateinit var followingAdapter: SuggestedAccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_followings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        followings = mutableListOf()
        fetchFollowingAccounts()
    }

    private fun fetchFollowingAccounts() {
        Firebase.database.getReference("users").child(Firebase.auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listOfFollowings = snapshot.child("listOfFollowings").value as MutableList<String>
                    for(uid in listOfFollowings){
                        getUserDetail(uid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun getUserDetail(uid: String) {
        Log.d("TAGG", "uid: "+uid)
        if (uid != ""){
            Firebase.database.getReference("users").child(uid)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("TAGG", "snapshot: "+snapshot)
                        val email = snapshot.child("email").value as String
                        val profileImageUrl = snapshot.child("profileImage").value as String
                        val userId = snapshot.child("uid").value as String
                        val user = SuggestedAccounts(profileImageUrl, email, userId)
                        followings.add(user)
                        followingAdapter = SuggestedAccountAdapter(followings, requireContext(), this@FollowingsFragment, "FollowingsFragment", findNavController())
                        binding.rvFollowings.adapter = followingAdapter
                        binding.rvFollowings.layoutManager = GridLayoutManager(requireContext(), 2)
                        Log.d("TAGG", "followings: "+followings.toString())
                        followingAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

    }

    override fun onFollowClick(uid: String) {
    }

    private fun updateToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Followings"
            // Set other toolbar properties if needed, such as menu items
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbar()
    }

}