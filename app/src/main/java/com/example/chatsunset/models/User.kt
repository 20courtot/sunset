package com.example.chatsunset.models

data class User(
    var uuid:String,
    val email: String,
    var pseudo :String,
    var image : String?
){
    constructor(): this("","","","")
}
