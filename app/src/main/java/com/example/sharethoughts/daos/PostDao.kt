package com.example.sharethoughts.daos

import com.example.sharethoughts.modles.Post
import com.example.sharethoughts.modles.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {
    private val db= FirebaseFirestore.getInstance()
    val postCollection=db.collection("posts")
    val auth= Firebase.auth

    fun addPost(text : String , state:Boolean){
        val currentUserId=auth.currentUser!!.uid
        GlobalScope.launch {
            val userDao=UserDao()
            val tempuser=userDao.getUserById(currentUserId).await().toObject(User::class.java)
            val currentTime=System.currentTimeMillis()
            val post= Post(text,tempuser?.uid.toString(),tempuser?.displayName,
                tempuser?.imageUrl.toString(),currentTime,state)
            postCollection.document().set(post)
        }
    }

    fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollection.document(postId).get()
    }

    fun deletePostById(postId:String){
        GlobalScope.launch {
            val currentUserId=auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)
            if(post?.uid==currentUserId){
                postCollection.document(postId).delete()
            }
        }
    }

    fun updateLikes(postId:String){
        GlobalScope.launch {
            val currentUserId=auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)
            val isLiked=post!!.likedBy.contains(currentUserId)
            if(isLiked){
                post.likedBy.remove(currentUserId)
            }else{
                post.likedBy.add(currentUserId)
            }
            postCollection.document(postId).set(post)
        }
    }
}