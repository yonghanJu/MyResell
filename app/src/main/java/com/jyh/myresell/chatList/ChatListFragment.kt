package com.jyh.myresell.chatList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.jyh.myresell.DBKey.Companion.CHILD_CHAT
import com.jyh.myresell.DBKey.Companion.DB_USERS
import com.jyh.myresell.R
import com.jyh.myresell.chatroom.ChatRoomActivity
import com.jyh.myresell.databinding.FragmentChatlistBinding
import com.jyh.myresell.model.ChatListItem

class ChatListFragment : Fragment(R.layout.fragment_chatlist) {
    private var _binding: FragmentChatlistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var chatListAdapter: ChatListAdapter
    private val chatRoomList = mutableListOf<ChatListItem>()
    private val auth by lazy { Firebase.auth }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatListAdapter = ChatListAdapter { chatListItem->
            context?.let{
                val intent = Intent(it, ChatRoomActivity::class.java)
                intent.putExtra("chatKey", chatListItem.key)
                startActivity(intent)
            }
        }
        chatRoomList.clear()
        binding.chatListRecyclerView.adapter = chatListAdapter
        binding.chatListRecyclerView.layoutManager = LinearLayoutManager(context)

        if (auth.currentUser == null) {
            return
        }

        val chatDB = Firebase.database.reference.child(DB_USERS).child(auth.currentUser!!.uid)
            .child(CHILD_CHAT)

        // ?????????????????? ??????-> ?????? ???????????? ???????????? ???????????? ????????? ???????????? ?????? ????????????
        // ???????????? -> ?????? ????????? ????????? ????????? ?????????, ????????? children.forEach ??????
        chatDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val model = it.getValue<ChatListItem>()
                    model ?: return
                    chatRoomList.add(model)
                }
                chatListAdapter.submitList(chatRoomList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}