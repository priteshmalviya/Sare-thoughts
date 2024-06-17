package com.example.sharethoughts.Activitys

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharethoughts.Adapter.PostAdapter
import com.example.sharethoughts.Adapter.PostItemClicked
import com.example.sharethoughts.R
import com.example.sharethoughts.daos.PostDao
import com.example.sharethoughts.modles.Post
import com.example.sharethoughts.modles.User
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileActivity : AppCompatActivity(), PostItemClicked {

    private lateinit var postAdapter : PostAdapter
    var mrecyclerview: RecyclerView? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var uid:String
    private val db= FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseAuth = FirebaseAuth.getInstance()

        val name=findViewById<TextView>(R.id.name)
        val image=findViewById<ImageView>(R.id.getuserimageinimageview)
        val email=findViewById<TextView>(R.id.email)
        val homebtn=findViewById<ImageView>(R.id.Homebtn)
        val logoutbtn=findViewById<ImageView>(R.id.LogOutBtn)

        uid=intent.getStringExtra("id").toString()


        val user = db.collection("users").document(uid)
        user.get().addOnSuccessListener { documentSnapshot ->
            val tempuser = documentSnapshot.toObject(User::class.java)
            name.text=tempuser?.displayName
            email.text=tempuser?.email
            Glide.with(this).load(tempuser?.imageUrl).circleCrop().into(image)
        }

        mrecyclerview=findViewById(R.id.ProfilePostview)
        setUpRecyclerView()

        logoutbtn.setOnClickListener {
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

        homebtn.setOnClickListener {
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        if(uid==firebaseAuth.currentUser?.uid){
            image.setOnClickListener {
                val intent=Intent(this, ChangeProfileActivity::class.java)
                finish()
                startActivity(intent)
            }
        }
    }

    private fun setUpRecyclerView() {
        val postCollections = FirebaseFirestore.getInstance().collection("posts")
        val query=postCollections.orderBy("createdAT", Query.Direction.DESCENDING).whereEqualTo("uid",uid)
        val recyclerViewOption=FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        postAdapter = PostAdapter(recyclerViewOption,this)

        mrecyclerview!!.adapter=postAdapter
        mrecyclerview!!.layoutManager= LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        postAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        postAdapter.stopListening()
    }

    override fun onPause() {
        super.onPause()
        if(firebaseAuth.currentUser !=null) {
            FirebaseDatabase.getInstance().getReference().child("Presence")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue("Offline")
        }
        finish()
    }


    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser !=null) {
            FirebaseDatabase.getInstance().getReference().child("Activity")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue("Profile")
            FirebaseDatabase.getInstance().getReference().child("Presence")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue("Online")
        }
    }



    override fun onlikeClicked(postId: String) {
        PostDao().updateLikes(postId)
    }

    override fun onDeleteClicked(postId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Post")
        builder.setMessage("Do You Want To Delete This Post")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            PostDao().deletePostById(postId)
            Toast.makeText(this,"Post Has Been Deleted", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->}
        builder.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent= Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}