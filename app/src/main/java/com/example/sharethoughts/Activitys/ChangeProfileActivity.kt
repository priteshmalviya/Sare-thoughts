package com.example.sharethoughts.Activitys

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.example.sharethoughts.R
import com.example.sharethoughts.modles.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.IOException

class ChangeProfileActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_profile)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage=FirebaseStorage.getInstance()
        storageReference=firebaseStorage!!.getReference()
        firebaseFirestore= FirebaseFirestore.getInstance()

        mgetuserimageinimageview=findViewById(R.id.profileImg)
        val cancle=findViewById<Button>(R.id.CancleBtn)
        msaveprofile=findViewById(R.id.SetBtn)
        mprogressbarofsetprofile=findViewById(R.id.progressBar)

        msaveprofile!!.isEnabled=false

        val user = firebaseFirestore!!.collection("users").document(firebaseAuth.uid.toString())
        user.get().addOnSuccessListener { documentSnapshot ->
            val tempuser = documentSnapshot.toObject(User::class.java)
            Glide.with(this).load(tempuser?.imageUrl).circleCrop().into(mgetuserimageinimageview!!)
        }

        mgetuserimageinimageview!!.setOnClickListener {
            intent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intent,PICK_IMAGE)
        }

        cancle.setOnClickListener{
            backToProfile()
        }

        msaveprofile!!.setOnClickListener{
                mprogressbarofsetprofile!!.setVisibility(View.VISIBLE)
                msaveprofile!!.isEnabled = false
                sendImagetoStorage()
        }
    }

    private fun sendImagetoStorage() {
        val imageref = storageReference!!.child("Images").child(firebaseAuth.uid!!).child("Profile Pic")

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
                sendDataTocloudFirestore()
            }.addOnFailureListener {
                Toast.makeText(applicationContext,"URI get Failed", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(applicationContext, "Image is uploaded", Toast.LENGTH_SHORT).show()
            mprogressbarofsetprofile!!.setVisibility(View.INVISIBLE)
        }.addOnFailureListener {
            Toast.makeText(applicationContext,"Image Not UPdloaded",Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendDataTocloudFirestore() {
        val documentReference = firebaseFirestore!!.collection("users").document(firebaseAuth.uid!!)
        val user=User(firebaseAuth.currentUser?.email.toString(),
                        firebaseAuth.currentUser?.uid.toString(),
                        firebaseAuth.currentUser?.displayName,
                        ImageUriAcessToken.toString())
        documentReference.set(user).addOnSuccessListener {
            Handler().postDelayed({
                backToProfile()
            }, 1000)
        }

        // update post data

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imagepath = data!!.data
            mgetuserimageinimageview!!.setImageURI(imagepath)
            msaveprofile!!.isEnabled=true
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    fun backToProfile(){
        val intent= Intent(this, ProfileActivity::class.java)
        intent.putExtra("id",firebaseAuth.currentUser?.uid.toString())
        finish()
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backToProfile()
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