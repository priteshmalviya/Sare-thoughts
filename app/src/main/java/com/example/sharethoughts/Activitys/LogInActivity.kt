package com.example.sharethoughts.Activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sharethoughts.R
import com.example.sharethoughts.daos.UserDao
import com.example.sharethoughts.modles.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LogInActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSinginClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        val  gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSinginClient= GoogleSignIn.getClient(this,gso)
        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.sininBtn).setOnClickListener{
            findViewById<ProgressBar>(R.id.progressBar).visibility= View.VISIBLE
            findViewById<Button>(R.id.sininBtn).visibility= View.GONE
            sinInGoogle()
        }
    }

    private fun sinInGoogle() {
        val sinInIntent=googleSinginClient.signInIntent
        launcher.launch(sinInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
        if(result.resultCode== RESULT_OK){
            val task=GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account:GoogleSignInAccount? = task.result
            if(account!=null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this,task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential=GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                divert()
            }else{
                findViewById<ProgressBar>(R.id.progressBar).visibility=View.GONE
                findViewById<Button>(R.id.sininBtn).visibility=View.VISIBLE
                Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            FirebaseDatabase.getInstance().reference.child("Token")
                .child(firebaseAuth.currentUser?.uid.toString()).setValue(token.toString())

        })
    }

    private fun divert() {
        insertToken()
        val user= User(firebaseAuth.currentUser?.email.toString(),
            firebaseAuth.currentUser?.uid.toString(),
            firebaseAuth.currentUser?.displayName,
            firebaseAuth.currentUser?.photoUrl.toString())
        val usersDao=UserDao()
        usersDao.addUser(user)
        val intent=Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser!=null){
            divert()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}