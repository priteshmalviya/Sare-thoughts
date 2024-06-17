package com.example.sharethoughts.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.sharethoughts.fregments.AddPostFregment
import com.example.sharethoughts.fregments.ChatFregment
import com.example.sharethoughts.fregments.HomeFregment

class PagerAdapter( fm: FragmentManager?, var tabcount: Int) :
    FragmentPagerAdapter(fm!!, tabcount) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ChatFregment()
            1 -> HomeFregment()
            else -> AddPostFregment()
        }
    }

    override fun getCount(): Int {
        return tabcount
    }
}