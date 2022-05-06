package com.jyh.myresell.chatroom

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jyh.myresell.DBKey.Companion.DB_CHATS
import com.jyh.myresell.databinding.ActivityChatRoomBinding

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private var chatDB: DatabaseReference? = null
    private val auth by lazy { Firebase.auth }
    private val chatList = mutableListOf<ChatItem>()
    private val chatItemAdapter = ChatItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initRecyclerView()
        initDB()
        initSendButton()
    }

    private fun initDB() {
        val chatKey = intent.getLongExtra("chatKey", -1)
        chatDB = Firebase.database.reference.child(DB_CHATS).child("$chatKey")
        chatDB?.addChildEventListener(object :ChildEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(ChatItem::class.java)
                chat?: return
                chatList.add(chat)
                chatItemAdapter.submitList(chatList)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun initSendButton() {
        binding.sendButton.setOnClickListener {
            val chat = ChatItem(
                auth.currentUser!!.uid,
                binding.messageEditText.text.toString()
            )
            chatDB?.push()?.setValue(chat)
        }
    }

    private fun initRecyclerView() {
        binding.chatListRecyclerView.adapter = chatItemAdapter
        binding.chatListRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}