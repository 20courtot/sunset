package com.example.chatsunset.models


data class Friend(
    var uuid : String,
    val pseudo:String,
    val lastMsg:String,
    val image:String,
    val timestamp: Long
){
    constructor() : this("","","","",0)
}
