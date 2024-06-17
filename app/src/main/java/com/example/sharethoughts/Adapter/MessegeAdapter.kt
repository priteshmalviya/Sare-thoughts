package com.example.sharethoughts.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharethoughts.R
import com.example.sharethoughts.modles.Messege
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class MessegeAdapter (val context: Context, val messegeList: ArrayList<Messege>, private val listener:MessegeClicked):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val ITEM_RECEIVE=1
    val ITEM_SENT=2
    val IMG_RECEIVE=3
    val IMG_SENT=4
    val DATE_CHANGED=5
    var lastdate:String=""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==1){
            val view: View =LayoutInflater.from(context).inflate(R.layout.receive,parent,false)
            val viewholder=ReceiveViewHolder(view)
            return viewholder
        }else if(viewType==2){
            val view: View = LayoutInflater.from(context).inflate(R.layout.send,parent,false)
            val viewholder= SentViewHolder(view)
            return viewholder
        }else if(viewType==3){
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive_image,parent,false)
            val viewholder= ReceiveImageViewHolder(view)
            view.setOnClickListener{
                //Toast.makeText(context,messegeList[viewholder.adapterPosition].messege.toString(),Toast.LENGTH_SHORT).show()
                listener.OpenBigImg(messegeList[viewholder.adapterPosition].messege.toString(),messegeList[viewholder.adapterPosition].isimg)
            }
            return viewholder
        }else{
            val view: View = LayoutInflater.from(context).inflate(R.layout.send_image,parent,false)
            val viewholder= SentImageViewHolder(view)
            view.setOnClickListener{
                //Toast.makeText(context,messegeList[viewholder.adapterPosition].messege.toString(),Toast.LENGTH_SHORT).show()
                listener.OpenBigImg(messegeList[viewholder.adapterPosition].messege.toString(),messegeList[viewholder.adapterPosition].isimg)
            }
            return viewholder
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessege=messegeList[position]

        if (holder.javaClass==SentViewHolder::class.java){
            val viewHolder=holder as SentViewHolder
            holder.timestamp.text=currentMessege.time
            if(currentMessege.isimg==true){
                holder.sentMessege.text="This Is An Image Click To Open."
            }else{
                holder.sentMessege.text=currentMessege.messege
            }
            holder.img.text=currentMessege.messege
            if(currentMessege.isseen==true){
                holder.isseen.text="Seen"
            }else{
                holder.isseen.text="Sent"
            }
            holder.date.text=currentMessege.date
            /*if(currentMessege.date.toString() != lastdate){
                holder.date.visibility=View.VISIBLE
                lastdate=currentMessege.date.toString()
            }*/
        }else if (holder.javaClass==ReceiveViewHolder::class.java){
            val viewHolder=holder as ReceiveViewHolder
            holder.timestamp.text=currentMessege.time
            if(currentMessege.isimg==true){
                holder.receiveMessege.text="This Is An Image Click To Open."
            }else{
                holder.receiveMessege.text=currentMessege.messege
            }
            holder.img.text=currentMessege.messege
            holder.date.text=currentMessege.date
            /*if(currentMessege.date.toString() != lastdate){
                holder.date.visibility=View.VISIBLE
                lastdate=currentMessege.date.toString()
            }*/
        }else if (holder.javaClass==SentImageViewHolder::class.java){
            val viewHolder=holder as SentImageViewHolder
            holder.timestamp.text=currentMessege.time
            Glide.with(holder.itemView.context).load(currentMessege.messege).into(holder.Sentimg)
            holder.date.text=currentMessege.date
            /*if(currentMessege.date.toString() != lastdate){
                holder.date.visibility=View.VISIBLE
                lastdate=currentMessege.date.toString()
            }*/
            if(currentMessege.isseen==true){
                holder.isseen.text="Seen"
            }else{
                holder.isseen.text="Sent"
            }
        }else if (holder.javaClass==ReceiveImageViewHolder::class.java){
            val viewHolder=holder as ReceiveImageViewHolder
            holder.timestamp.text=currentMessege.time
            Glide.with(holder.itemView.context).load(currentMessege.messege).into(holder.Receiveimg)
            holder.date.text=currentMessege.date
            /*if(currentMessege.date.toString() != lastdate){
                holder.date.visibility=View.VISIBLE
                lastdate=currentMessege.date.toString()
            }*/
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessege=messegeList[position]

        /*if(currentMessege.date.toString() != lastdate){
            return DATE_CHANGED
        }*/

        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessege.senderId)){
            if(currentMessege.isimg){
                return IMG_SENT
            }
            return ITEM_SENT
        }else{
            if(currentMessege.isimg){
                return IMG_RECEIVE
            }
            return ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messegeList.size
    }


    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val sentMessege=itemView.findViewById<TextView>(R.id.txt_send_message)
        val timestamp=itemView.findViewById<TextView>(R.id.time)
        val img=itemView.findViewById<TextView>(R.id.img_send_message)
        val isseen=itemView.findViewById<TextView>(R.id.isseen)
        val date=itemView.findViewById<TextView>(R.id.date)
    }
    class ReceiveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receiveMessege=itemView.findViewById<TextView>(R.id.txt_receive_message)
        val timestamp=itemView.findViewById<TextView>(R.id.time)
        val img=itemView.findViewById<TextView>(R.id.img_receive_message)
        val date=itemView.findViewById<TextView>(R.id.date)
    }
    class ReceiveImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val timestamp=itemView.findViewById<TextView>(R.id.time)
        val Receiveimg=itemView.findViewById<ImageView>(R.id.img_receive_message)
        val date=itemView.findViewById<TextView>(R.id.date)
        val ProgressBar=itemView.findViewById<ProgressBar>(R.id.progressBar2)
    }
    class SentImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val timestamp=itemView.findViewById<TextView>(R.id.time)
        val Sentimg=itemView.findViewById<ImageView>(R.id.img_send_message)
        val date=itemView.findViewById<TextView>(R.id.date)
        val isseen=itemView.findViewById<TextView>(R.id.isseen)
        val ProgressBar=itemView.findViewById<ProgressBar>(R.id.progressBar2)
    }
    class DateChangedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val date=itemView.findViewById<TextView>(R.id.date)
    }
}

interface MessegeClicked {
    fun OpenBigImg(toString: String, isimg: Boolean)
}