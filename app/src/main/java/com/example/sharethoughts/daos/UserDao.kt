package com.example.sharethoughts.daos

import com.example.sharethoughts.modles.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDao {

    private val db=FirebaseFirestore.getInstance()
    val usersCollection=db.collection("users")

    fun addUser(user: User?){
        user?.let {
            GlobalScope.launch {
                val tempuser=getUserById(Firebase.auth.currentUser!!.uid).await().toObject(User::class.java)
                if (tempuser?.uid==null){
                    usersCollection.document(user.uid).set(user)
                }
            }
        }
    }

    fun getUserById(uId:String):Task<DocumentSnapshot>{
        return usersCollection.document(uId).get()
    }

}