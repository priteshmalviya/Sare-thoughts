package com.example.sharethoughts.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharethoughts.R
import com.example.sharethoughts.others.Utils
import com.example.sharethoughts.modles.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PostAdapter (options: FirestoreRecyclerOptions<Post>, val listener : PostItemClicked) : FirestoreRecyclerAdapter<Post, PostAdapter.PostviewHolder>(options){

    class PostviewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val uploaderimg: ImageView = itemView.findViewById(R.id.uploaderimg)
        val uploadername: TextView = itemView.findViewById(R.id.uploadername)
        val createdAt: TextView = itemView.findViewById(R.id.createdAt)
        val postTitle: TextView = itemView.findViewById(R.id.posttitle)
        val likecount: TextView =itemView.findViewById(R.id.likecount)
        val postimg: ImageView =itemView.findViewById(R.id.Postedimg)
        val likebtn: ImageView =itemView.findViewById(R.id.likebtn)
        val deletebtn: Button =itemView.findViewById(R.id.deletebtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostviewHolder {
        val viewholder= PostviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post,parent,false))
        viewholder.likebtn.setOnClickListener {
            listener.onlikeClicked(snapshots.getSnapshot(viewholder.absoluteAdapterPosition).id)
        }
        viewholder.deletebtn.setOnClickListener{
            listener.onDeleteClicked(snapshots.getSnapshot(viewholder.absoluteAdapterPosition).id)
        }
        return viewholder
    }

    override fun onBindViewHolder(holder: PostviewHolder, position: Int, model: Post) {
        val auth= Firebase.auth
        if(model.state) {
            holder.postimg.visibility= View.VISIBLE
            holder.postTitle.visibility= View.GONE
            Glide.with(holder.postimg.context).load(model.text).into(holder.postimg)
        }else{
            holder.postTitle.text = model.text
        }
        holder.uploadername.text=model.displayName
        holder.createdAt.text= Utils.getTimeAgo(model.createdAT)
        Glide.with(holder.uploaderimg.context).load(model.imageUrl).circleCrop().into(holder.uploaderimg)
        holder.likecount.text=model.likedBy.size.toString()

        val currentUserId=auth.currentUser!!.uid
        val isLiked=model.likedBy.contains(currentUserId)
        if(isLiked){
            holder.likebtn.setImageDrawable(ContextCompat.getDrawable(holder.likebtn.context,R.drawable.ic_baseline_liked))
        }else{
            holder.likebtn.setImageDrawable(ContextCompat.getDrawable(holder.likebtn.context,R.drawable.ic_baseline_favorite_border_24))
        }
        if(currentUserId==model.uid){
            holder.deletebtn.visibility= View.VISIBLE
        }
    }


}

interface PostItemClicked{
    //fun onProfileClicked(userId : String)
    fun onlikeClicked(postId : String)
    fun onDeleteClicked(postId : String)
}