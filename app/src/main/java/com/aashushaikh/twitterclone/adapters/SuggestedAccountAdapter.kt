package com.aashushaikh.twitterclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.aashushaikh.twitterclone.R
import com.aashushaikh.twitterclone.data.SuggestedAccounts
import com.aashushaikh.twitterclone.fragments.FollowingsFragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import de.hdodenhof.circleimageview.CircleImageView

class SuggestedAccountAdapter(
    private val suggestedAccounts: List<SuggestedAccounts>,
    private val context: Context,
    private val onFollowClickListener: OnFollowClickListener,
    private val calledBy: String,
    private val navController: NavController? = null
) : RecyclerView.Adapter<SuggestedAccountAdapter.SuggestedAccountViewHolder>() {

    inner class SuggestedAccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val suggestedAccProfile = itemView.findViewById<CircleImageView>(R.id.img_suggested_profile)
        val suggestedAccEmail = itemView.findViewById<TextView>(R.id.tv_suggested_email)
        val btnFollow = itemView.findViewById<MaterialButton>(R.id.btn_follow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedAccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_suggested_account, parent, false)
        return SuggestedAccountViewHolder(view)
    }

    override fun getItemCount(): Int {
        return suggestedAccounts.size
    }

    override fun onBindViewHolder(holder: SuggestedAccountViewHolder, position: Int) {
        val currentAccount = suggestedAccounts[position]
        holder.suggestedAccEmail.text = currentAccount.suggestedAccEmail
        Glide.with(context).load(currentAccount.suggestedAccProfile).into(holder.suggestedAccProfile)

        if (calledBy == "FollowingsFragment") {
            holder.btnFollow.text = "Message"
            holder.btnFollow.setOnClickListener {
                val bundle = bundleOf(
                    "email" to suggestedAccounts[position].suggestedAccEmail,
                    "profile" to suggestedAccounts[position].suggestedAccProfile,
                    "userId" to suggestedAccounts[position].uid
                )
                onFollowClickListener.onFollowClick(suggestedAccounts[position].uid)
                navController?.navigate(R.id.action_followingsFragment_to_chatsFragment, bundle)
            }
        } else {
            holder.btnFollow.text = "Follow"
            holder.btnFollow.setOnClickListener {
                onFollowClickListener.onFollowClick(suggestedAccounts[position].uid)
            }
        }

    }

}

interface OnFollowClickListener {
    fun onFollowClick(uid: String)
}