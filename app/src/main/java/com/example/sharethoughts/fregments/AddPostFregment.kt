package com.example.sharethoughts.fregments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sharethoughts.R
import com.example.sharethoughts.daos.PostDao

class AddPostFregment: Fragment() {


    private lateinit var postDao: PostDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.addpostfregment, container, false)


        postDao= PostDao()

        v.findViewById<Button>(R.id.postbtn).setOnClickListener{
            val input=v.findViewById<EditText>(R.id.postInput).text.toString()
            v.findViewById<EditText>(R.id.postInput).text.clear()
            if(input.isNotEmpty()){
                postDao.addPost(input,false)
                Toast.makeText(v.context, "Your post Has Been Posted.", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(v.context,"Please Write Somthing.",Toast.LENGTH_LONG).show()
            }
        }

        return v
    }


}