package com.jyh.myresell.chatroom

data class ChatItem (
    val senderId: String,
    val message:String
){
    constructor():this("","")
}