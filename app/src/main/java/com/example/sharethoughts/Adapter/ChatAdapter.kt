package com.example.sharethoughts.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharethoughts.R
import com.example.sharethoughts.modles.Messege
import com.example.sharethoughts.modles.User
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


val mDbRef= FirebaseDatabase.getInstance().getReference()

class ChatAdapter (options: FirestoreRecyclerOptions<User>, val listener : MessegeItemClicked) : FirestoreRecyclerAdapter<User, ChatAdapter.ChatviewHolder>(options) {

    class ChatviewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val image:ImageView=itemView.findViewById(R.id.userimg)
        val name:TextView= itemView.findViewById(R.id.username)
        val lastmessege:TextView=itemView.findViewById(R.id.lastmessege)
        val timstamp:TextView=itemView.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.userinchatsection, parent, false)
        val viewholder=ChatviewHolder(view)
        view.setOnClickListener {
            listener.onProfileClicked(snapshots.getSnapshot(viewholder.absoluteAdapterPosition).get("uid").toString())
        }
        return viewholder
    }

    override fun onBindViewHolder(holder: ChatviewHolder, position: Int, model: User) {
        val senderRoom=Firebase.auth.uid.toString()+model.uid
        holder.name.text=model.displayName
        Glide.with(holder.image.context).load(model.imageUrl).circleCrop().into(holder.image)
        mDbRef.child("chats").child(senderRoom).child("Lastmessege").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val messege=snapshot.getValue(Messege::class.java)
                    var msg=messege?.messege.toString()
                    if(messege!!.isimg){
                        msg="Photo."
                    }
                    if(messege.senderId==Firebase.auth.uid.toString()) {
                        holder.lastmessege.text = "You: "+msg
                    }else{
                        holder.lastmessege.text = msg
                        if(messege.isseen==false){
                            holder.lastmessege.setTextColor(Color.parseColor("#19e319"))
                        }else{
                            holder.lastmessege.setTextColor(Color.parseColor("#000000"))
                        }
                    }
                    holder.timstamp.text=messege.time
                }else{
                    holder.lastmessege.text="No Messeges Yet."
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

interface MessegeItemClicked{
    fun onProfileClicked(uid : String)
}