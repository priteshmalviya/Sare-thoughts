package com.example.sharethoughts.Activitys

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.sharethoughts.Adapter.PagerAdapter
import com.example.sharethoughts.R
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {


    private lateinit var firebaseAuth: FirebaseAuth
    var tabLayout: TabLayout? = null
    var mchat: TabItem? = null
    var maddpost: TabItem? = null
    var mhome: TabItem? = null
    var viewPager: ViewPager? = null
    var pagerAdapter: PagerAdapter? = null
    var mtoolbar: Toolbar? = null
    private lateinit var logOut: ImageView
    private lateinit var mprofile:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //FirebaseMessaging.getInstance().subscribeToTopic("/topics/pritesh")

        firebaseAuth = FirebaseAuth.getInstance()

        tabLayout=findViewById(R.id.include)
        mchat=findViewById(R.id.chat)
        mhome=findViewById(R.id.home)
        maddpost=findViewById(R.id.addpost)
        viewPager=findViewById(R.id.fragmentcontainer)
        logOut=findViewById(R.id.LogOutBtn)
        mprofile=findViewById(R.id.Profilebtn)

        mtoolbar=findViewById(R.id.toolbar)
        setSupportActionBar(mtoolbar)

        pagerAdapter= PagerAdapter(supportFragmentManager,tabLayout!!.tabCount)
        viewPager!!.adapter=pagerAdapter

        tabLayout!!.setOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
                if (viewPager!!.currentItem == 0 || viewPager!!.currentItem == 1 || viewPager!!.currentItem == 2 || viewPager!!.currentItem == 3) {
                    pagerAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        logOut.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Log Out")
            builder.setMessage("Do You Want To Logout")
            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                FirebaseDatabase.getInstance().getReference().child("Presence").child(firebaseAuth.currentUser?.uid.toString()).setValue("Logged Out")
                firebaseAuth.signOut()
                finish()
                startActivity(Intent(this, LogInActivity::class.java))
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->}
            builder.show()
        }

        mprofile.setOnClickListener {
            val intent=Intent(this, ProfileActivity::class.java)
            intent.putExtra("id",firebaseAuth.currentUser?.uid.toString())
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser !=null) {
            FirebaseDatabase.getInstance().getReference().child("Presence")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue("Online")
            FirebaseDatabase.getInstance().getReference().child("Activity")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue("Main")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")
        val formatted = current.format(formatter)
        Toast.makeText(this, formatted, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        if(firebaseAuth.currentUser !=null) {
            FirebaseDatabase.getInstance().getReference().child("Presence")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue("Offline")
        }
        finish()
    }

}