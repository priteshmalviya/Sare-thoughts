package com.example.sharethoughts.modles


class Messege {
    var messege:String?=null
    var senderId:String?=null
    var date:String?=null
    var time:String?=null
    var isimg:Boolean=false
    var isseen:Boolean=false

    constructor(){}

    constructor(messege:String?,senderId:String?,date:String?,time: String?,isimg:Boolean,isseen:Boolean){
        this.messege=messege
        this.senderId=senderId
        this.date=date
        this.time=time
        this.isimg=isimg
        this.isseen=isseen
    }

}