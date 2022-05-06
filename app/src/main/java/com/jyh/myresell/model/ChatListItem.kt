package com.jyh.myresell.model

data class ChatListItem (
    val sellerId:String,
    val buyerId:String,
    val itemTitle:String,
    val key:Long
){
    constructor() : this("","","",0)
}