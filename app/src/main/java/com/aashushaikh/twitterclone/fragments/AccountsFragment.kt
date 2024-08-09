package com.aashushaikh.twitterclone.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.aashushaikh.twitterclone.R
import com.aashushaikh.twitterclone.adapters.OnFollowClickListener
import com.aashushaikh.twitterclone.adapters.SuggestedAccountAdapter
import com.aashushaikh.twitterclone.data.SuggestedAccounts
import com.aashushaikh.twitterclone.data.User
import com.aashushaikh.twitterclone.databinding.FragmentAccountsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

class AccountsFragment : Fragment(), OnFollowClickListener {

    private lateinit var binding: FragmentAccountsBinding
    private lateinit var suggestedAccounts: MutableList<SuggestedAccounts>
    private lateinit var suggestedAccountAdapter: SuggestedAccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_accounts, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        suggestedAccounts = mutableListOf()
        fetchAllAccounts()
    }

    private fun fetchAllAccounts() {
        Firebase.database.getReference("users").child(Firebase.auth.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listOfFollowings = snapshot.child("listOfFollowings").value as MutableList<String>
                    Log.d("TAGG", "list of followings: "+listOfFollowings.toString())
                    fetchSuggestedAccounts(listOfFollowings)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun fetchSuggestedAccounts(listOfFollowings: List<String>) {
        Firebase.database.getReference("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return // Check if the fragment is still added to the activity
                    suggestedAccounts.clear()
                    for (dataSnapshot in snapshot.children) {
                        val user: User? = dataSnapshot.getValue(User::class.java)
                        if (user != null && user.uid != Firebase.auth.uid && user.uid !in listOfFollowings) {
                            val suggestedUser = SuggestedAccounts(
                                suggestedAccProfile = user.profileImage,
                                suggestedAccEmail = user.email,
                                uid = user.uid
                            )
                            suggestedAccounts.add(suggestedUser)
                        }
                    }
                    suggestedAccountAdapter = SuggestedAccountAdapter(suggestedAccounts, requireContext(), this@AccountsFragment, "AccountsFragment")
                    binding.suggestedAccRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.suggestedAccRv.adapter = suggestedAccountAdapter
                    suggestedAccountAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }

    override fun onFollowClick(uid: String) {
        followUser(uid)
    }

    private fun followUser(uid: String) {
        Firebase.database.getReference("users").child(Firebase.auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listOfFollowings = snapshot.child("listOfFollowings").value as? MutableList<String>
                    listOfFollowings?.add(uid)

                    Firebase.database.getReference("users").child(Firebase.auth.currentUser!!.uid).child("listOfFollowings")
                        .setValue(listOfFollowings)

                    Toast.makeText(requireContext(), "User followed", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}