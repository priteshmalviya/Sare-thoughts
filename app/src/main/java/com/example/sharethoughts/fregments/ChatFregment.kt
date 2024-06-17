package com.example.sharethoughts.fregments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharethoughts.Adapter.ChatAdapter
import com.example.sharethoughts.Adapter.MessegeItemClicked
import com.example.sharethoughts.Activitys.ChatActivity
import com.example.sharethoughts.R
import com.example.sharethoughts.modles.User
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ChatFregment: Fragment(), MessegeItemClicked {

    private var firebaseFirestore: FirebaseFirestore? = null
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var chatAdapter : ChatAdapter
    var mrecyclerview: RecyclerView? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.chatfregment, container, false)


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        mrecyclerview=v.findViewById(R.id.recyclerview)


        setUpRecyclerView()

        return v
    }

    private fun setUpRecyclerView() {
        val userCollections = firebaseFirestore!!.collection("users")
        val query=userCollections.whereNotEqualTo("uid", firebaseAuth!!.uid)
        val recyclerViewOption=FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java).build()

        chatAdapter = ChatAdapter(recyclerViewOption,this)

        mrecyclerview!!.adapter=chatAdapter
        mrecyclerview!!.layoutManager= LinearLayoutManager(this.context)
    }

    override fun onStart() {
        super.onStart()
        chatAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        chatAdapter.stopListening()
    }

    override fun onProfileClicked(uid: String) {
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra("id", uid)
        startActivity(intent)
    }


}