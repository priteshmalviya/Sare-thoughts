package com.example.sharethoughts.Activitys

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.sharethoughts.R
import com.example.sharethoughts.others.SendNotification
import com.example.sharethoughts.modles.Messege
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SendImageMessegeActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var PICK_IMAGE = 123
    private var mgetuserimageinimageview: ImageView? = null
    private var imagepath: Uri? = null
    private var msaveprofile: Button? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var ImageUriAcessToken: String? = null
    private var firebaseFirestore: FirebaseFirestore? = null
    var mprogressbarofsetprofile: ProgressBar? = null
    var receverid: String? = null
    val senderUid = FirebaseAuth.getInstance().currentUser?.uid
    var receiverRoom: String? = null
    var senderRoom: String? = null
    val mDbRef = FirebaseDatabase.getInstance().getReference()



    var senderToken:String?=null
    var receiverToken:String?=null
    var status:String?=null


    var receverOnChat:Boolean=false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_image_messege)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage!!.getReference()
        firebaseFirestore = FirebaseFirestore.getInstance()

        mgetuserimageinimageview = findViewById(R.id.profileImg)
        val cancle = findViewById<Button>(R.id.CancleBtn)
        msaveprofile = findViewById(R.id.SetBtn)
        mprogressbarofsetprofile = findViewById(R.id.progressBar)


        receverid = intent.getStringExtra("id")

        senderRoom = receverid + senderUid
        receiverRoom = senderUid + receverid

        msaveprofile!!.isEnabled = false

        mgetuserimageinimageview!!.setOnClickListener {
            intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        cancle.setOnClickListener {
            backToChat()
        }

        msaveprofile!!.setOnClickListener{
            mprogressbarofsetprofile!!.setVisibility(View.VISIBLE)
            msaveprofile!!.isEnabled = false
            sendImagetoStorage()
        }

        mDbRef.child("Activity").child(receverid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val position = snapshot.getValue(String::class.java)
                        receverOnChat = position=="Chat"+senderRoom
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        mDbRef.child("Presence").child(receverid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val stat = snapshot.getValue(String::class.java)
                        status = stat.toString()
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

        mDbRef.child("Token").child(receverid.toString())
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


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendImagetoStorage() {
        val imageref = storageReference!!.child("Messeges").child(Date().time.toString())

        //Image compresesion
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imagepath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val data: ByteArray = byteArrayOutputStream.toByteArray()

        ///putting image to storage
        val uploadTask = imageref.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageref.downloadUrl.addOnSuccessListener { uri ->
                ImageUriAcessToken = uri.toString()
                sendDataToRealtimeDatabase()
            }.addOnFailureListener {
                Toast.makeText(applicationContext, "URI get Failed", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(applicationContext, "Image is uploaded", Toast.LENGTH_SHORT).show()
            mprogressbarofsetprofile!!.setVisibility(View.INVISIBLE)
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "Image Not UPdloaded", Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendDataToRealtimeDatabase() {
        val current = LocalDateTime.now()
        var formatted = current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        //Toast.makeText(this, formatted, Toast.LENGTH_SHORT).show()
        val date : String=formatted
        formatted = current.format(DateTimeFormatter.ofPattern("HH:mm"))
        val time:String= formatted
        val messageObject = Messege(ImageUriAcessToken.toString(), senderUid, date,time, true,status!="Logged Out" && status!="Offline" && receverOnChat)
        //val messageObject1 = Messege(imagepath.toString(), senderUid, Date(), true)

        mDbRef.child("chats").child(senderRoom!!).child("messeges").push()
            .setValue(messageObject).addOnSuccessListener {
                mDbRef.child("chats").child(receiverRoom!!).child("messeges").push()
                    .setValue(messageObject)
                mDbRef.child("chats").child(senderRoom!!).child("Lastmessege")
                    .setValue(messageObject)
                mDbRef.child("chats").child(receiverRoom!!).child("Lastmessege")
                    .setValue(messageObject)
            }

        Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        if(status=="Offline" || (status!="Logged Out" && !receverOnChat)) {
            sendNotification()
        }
        backToChat()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imagepath = data!!.data
            mgetuserimageinimageview!!.setImageURI(imagepath)
            msaveprofile!!.isEnabled = true
        }
        super.onActivityResult(requestCode, resultCode, data)
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
            data.put("messege","Photo")


            val body = JSONObject()
            body.put(SendNotification.REMOTE_MSG_DATA,data)
            body.put(SendNotification.REMOTE_MSG_REGISTRATION_IDS,token)

            val call = SendNotification(this)
            call.sendNotification(body.toString())

        }catch (e : Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun backToChat() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("id", receverid)
        finish()
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backToChat()
    }

    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser !=null) {
            FirebaseDatabase.getInstance().getReference().child("Presence")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue("Online")
        }
    }

    override fun onPause() {
        super.onPause()
        if(firebaseAuth.currentUser !=null) {
            FirebaseDatabase.getInstance().getReference().child("Presence")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue("Offline")
        }
    }

}