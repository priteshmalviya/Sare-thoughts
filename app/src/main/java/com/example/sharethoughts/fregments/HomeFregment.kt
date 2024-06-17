package com.example.sharethoughts.fregments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharethoughts.Adapter.PostAdapter
import com.example.sharethoughts.Adapter.PostItemClicked
import com.example.sharethoughts.R
import com.example.sharethoughts.daos.PostDao
import com.example.sharethoughts.modles.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFregment: Fragment(),PostItemClicked{

    private lateinit var postAdapter : PostAdapter
    var mrecyclerview: RecyclerView? = null
    private lateinit var v : View


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        v = inflater.inflate(R.layout.homefregment, container, false)


        mrecyclerview=v.findViewById(R.id.recyclerview)
        setUpRecyclerView()


        return v
    }

    private fun setUpRecyclerView() {
        val postCollections = FirebaseFirestore.getInstance().collection("posts")
        val query=postCollections.orderBy("createdAT", Query.Direction.DESCENDING)
        val recyclerViewOption=
            FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        postAdapter = PostAdapter(recyclerViewOption,this)

        mrecyclerview!!.adapter=postAdapter
        mrecyclerview!!.layoutManager= LinearLayoutManager(this.context)
    }

    override fun onStart() {
        super.onStart()
        postAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        postAdapter.stopListening()
    }

    override fun onlikeClicked(postId: String) {
        PostDao().updateLikes(postId)
    }

    override fun onDeleteClicked(postId: String) {
        val builder = AlertDialog.Builder(v.context)
        builder.setTitle("Delete Post")
        builder.setMessage("Do You Want To Delete This Post")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            PostDao().deletePostById(postId)
            Toast.makeText(v.context,"Post Has Been Deleted", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->}
        builder.show()
    }

}