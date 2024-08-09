package com.aashushaikh.twitterclone.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aashushaikh.twitterclone.fragments.AccountsFragment
import com.aashushaikh.twitterclone.fragments.TweetsFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> AccountsFragment()
            1 -> TweetsFragment()
            else -> AccountsFragment()
        }
    }
}