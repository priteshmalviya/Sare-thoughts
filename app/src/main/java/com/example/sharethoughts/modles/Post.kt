package com.example.sharethoughts.modles

class Post (
    val text:String="",
    val uid:String="",
    val displayName:String?="",
    val imageUrl:String="",
    val createdAT: Long=0L,
    val state:Boolean=false,
    val likedBy:ArrayList<String> = ArrayList()
)