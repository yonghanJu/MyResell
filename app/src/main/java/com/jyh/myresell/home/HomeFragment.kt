package com.jyh.myresell.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jyh.myresell.DBKey.Companion.CHILD_CHAT
import com.jyh.myresell.DBKey.Companion.DB_ARTICLES
import com.jyh.myresell.DBKey.Companion.DB_USERS
import com.jyh.myresell.R
import com.jyh.myresell.databinding.FragmentHomeBinding
import com.jyh.myresell.model.ArticleModel
import com.jyh.myresell.model.ChatListItem

class HomeFragment:Fragment(R.layout.fragment_home) {

    private lateinit var articleAdapter :ArticleAdapter
    private lateinit var articleDB:DatabaseReference
    private lateinit var userDB:DatabaseReference

    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object :ChildEventListener{
        @SuppressLint("NotifyDataSetChanged")
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return
            articleList.add(articleModel)
            articleAdapter.submitList(articleList)
            articleAdapter.notifyDataSetChanged()
        }
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}

    }

    private var _binding : FragmentHomeBinding? = null
    private val binding get()=_binding!!
    private val auth by lazy{
        Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addFloatingButton.setOnClickListener {
            if(auth.currentUser==null){
                Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_SHORT).show()
            }else{
                //startActivity(Intent(requireContext(), AddArticleActivity::class.java))
            }
            startActivity(Intent(requireContext(), AddArticleActivity::class.java))
        }

        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        userDB = Firebase.database.reference.child(DB_USERS)
        articleDB.addChildEventListener(listener)

        articleAdapter= ArticleAdapter { articleModel->
            if(auth.currentUser !=null){
                if(auth.currentUser!!.uid != articleModel.sellerId){
                    val chatRoom = ChatListItem(
                        articleModel.sellerId,
                        auth.currentUser!!.uid,
                        articleModel.title,
                        System.currentTimeMillis()
                    )

                    userDB.child(auth.currentUser!!.uid)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)
                    userDB.child(articleModel.sellerId)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)

                    Snackbar.make(binding!!.root, "채팅방 생성", Snackbar.LENGTH_SHORT).show()
                }
            }else{

            }
        }
        binding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.articleRecyclerView.adapter = articleAdapter
        articleAdapter.submitList(articleList)
        articleAdapter.notifyDataSetChanged()
        articleList.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        articleDB.removeEventListener(listener)
        _binding=null
    }
}