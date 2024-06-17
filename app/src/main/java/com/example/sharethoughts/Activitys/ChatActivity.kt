package com.example.sharethoughts.Activitys

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharethoughts.Adapter.MessegeAdapter
import com.example.sharethoughts.Adapter.MessegeClicked
import com.example.sharethoughts.R
import com.example.sharethoughts.others.SendNotification
import com.example.sharethoughts.modles.Messege
import com.example.sharethoughts.modles.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class ChatActivity : AppCompatActivity(), MessegeClicked {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messegeBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messegeAdapter: MessegeAdapter
    private lateinit var messsgeList:ArrayList<Messege>
    private lateinit var mDbRef : DatabaseReference
    private val db= FirebaseFirestore.getInstance()
    val senderUid= FirebaseAuth.getInstance().currentUser?.uid

    var receiverRoom:String ?= null
    var senderRoom:String ?= null

    var senderToken:String?=null
    var receiverToken:String?=null

    var receverOnChat:Boolean=false


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val Img = findViewById<ImageView>(R.id.userimg)
        val name = findViewById<TextView>(R.id.username)
        val lastseen = findViewById<TextView>(R.id.lastseen)

        val receiverUid = intent.getStringExtra("id")

        val user = db.collection("users").document(receiverUid.toString())
        user.get().addOnSuccessListener { documentSnapshot ->
            val tempuser = documentSnapshot.toObject(User::class.java)
            name.text = tempuser?.displayName
            Glide.with(this).load(tempuser?.imageUrl).circleCrop().into(Img)
        }

        Img.setOnClickListener {
            gotoProfile(receiverUid.toString())
        }
        name.setOnClickListener {
            gotoProfile(receiverUid.toString())
        }


        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid



        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messegeBox = findViewById(R.id.messegeinput)
        sendButton = findViewById(R.id.sendbutton)

        messsgeList = ArrayList()
        messegeAdapter = MessegeAdapter(this, messsgeList,this)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messegeAdapter

        mDbRef.child("Activity").child(receiverUid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val position = snapshot.getValue(String::class.java)
                        receverOnChat = position=="Chat"+senderRoom
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
        })

        mDbRef.child("Presence").child(receiverUid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        lastseen.text = status
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })



        //   geting Tokns     ////

        mDbRef.child("Token").child(senderUid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        senderToken=status
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        mDbRef.child("Token").child(receiverUid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        receiverToken=status
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })


        //   geting Tokns     ////

        val handler = Handler()

        messegeBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                mDbRef.child("Presence").child(senderUid.toString()).setValue("Typing....")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStopedTyping, 500)
            }

            var userStopedTyping = Runnable {
                mDbRef.child("Presence").child(senderUid.toString()).setValue("Online")
            }

        })

        mDbRef.child("chats").child(senderRoom!!).child("messeges")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messsgeList.clear()
                    for (postSnapshot in snapshot.children) {
                        val messege = postSnapshot.getValue(Messege::class.java)
                        messsgeList.add(messege!!)
                    }
                    messegeAdapter.notifyDataSetChanged()
                    chatRecyclerView.smoothScrollToPosition(messegeAdapter.getItemCount())
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        sendButton.setOnClickListener {
            findViewById<ImageView>(R.id.OpenImageInChat).visibility=View.GONE
            findViewById<ImageView>(R.id.closebtn).visibility=View.GONE
            val message = messegeBox.text.toString()
            val current = LocalDateTime.now()
            var formatted = current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val date : String=formatted
            formatted = current.format(DateTimeFormatter.ofPattern("HH:mm"))
            val time:String= formatted
            if (message.isNotEmpty()) {
                val messageObject = Messege(message, senderUid, date, time, false,lastseen.text!="Logged Out" && receverOnChat && lastseen.text!="Offline")

                mDbRef.child("chats").child(senderRoom!!).child("messeges").push()
                    .setValue(messageObject).addOnSuccessListener {
                        mDbRef.child("chats").child(receiverRoom!!).child("messeges").push()
                            .setValue(messageObject)
                        mDbRef.child("chats").child(senderRoom!!).child("Lastmessege")
                            .setValue(messageObject)
                        mDbRef.child("chats").child(receiverRoom!!).child("Lastmessege")
                            .setValue(messageObject)
                    }
                if((!receverOnChat && lastseen.text!="Logged Out")|| lastseen.text=="Offline"){
                    sendNotification()
                }
                messegeBox.setText("")
            } else {
                Toast.makeText(this, "Please Write Somthing.", Toast.LENGTH_SHORT).show()
            }

        }

        findViewById<ImageView>(R.id.camerabtn).setOnClickListener {
            val intent=Intent(this, SendImageMessegeActivity::class.java)
            intent.putExtra("id",receiverUid)
            startActivity(intent)
            //Toast.makeText(this, "This Function Is Temporary Disable.", Toast.LENGTH_SHORT).show()
        }


        findViewById<ImageView>(R.id.closebtn).setOnClickListener {
            findViewById<ImageView>(R.id.OpenImageInChat).visibility=View.GONE
            findViewById<ImageView>(R.id.closebtn).visibility=View.GONE
        }

    }

    private fun sendNotification() {
        //Toast.makeText(this,"Token : "+senderToken,Toast.LENGTH_LONG).show()

        try {
            val token = JSONArray()
            token.put(receiverToken)

            val data = JSONObject()
            data.put("userId",senderUid)
            data.put("name",FirebaseAuth.getInstance().currentUser?.displayName.toString())
            data.put("fcmToken",senderToken)
            data.put("messege",messegeBox.text)


            val body = JSONObject()
            body.put(SendNotification.REMOTE_MSG_DATA,data)
            body.put(SendNotification.REMOTE_MSG_REGISTRATION_IDS,token)

            val call = SendNotification(this)
            call.sendNotification(body.toString())

        }catch (e : Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun gotoProfile(uid:String){
        val intent=Intent(this, ProfileActivity::class.java)
        intent.putExtra("id",uid)
        finish()
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent=Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        setseen()
        FirebaseDatabase.getInstance().getReference().child("Presence").child(senderUid.toString()).setValue("Offline")
    }

    override fun onResume() {
        super.onResume()
        setseen()
        Upadatedata()
        FirebaseDatabase.getInstance().getReference().child("Activity")
            .child(senderUid.toString()).setValue("Chat"+receiverRoom)
        FirebaseDatabase.getInstance().getReference().child("Presence").child(senderUid.toString()).setValue("Online")
    }

    private fun setseen() {
        mDbRef.child("chats").child(receiverRoom!!).child("Lastmessege").child("isseen").setValue(true)
    }

    override fun OpenBigImg(path: String, isimg: Boolean) {
        val bigImf=findViewById<ImageView>(R.id.OpenImageInChat)
        if(isimg){
            Glide.with(this).load(path).into(bigImf)
            bigImf.visibility=View.VISIBLE
            findViewById<ImageView>(R.id.closebtn).visibility=View.VISIBLE
        }
    }

    fun Upadatedata(){
        val applesQuery = mDbRef.child("chats").child(receiverRoom!!).child("messeges").orderByChild("isseen").equalTo(false)

        applesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    appleSnapshot.ref.child("isseen").setValue(true)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, databaseError.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }
}