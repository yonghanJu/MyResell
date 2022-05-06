package com.jyh.myresell.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jyh.myresell.R
import com.jyh.myresell.databinding.FragementMypageBinding

class MyPageFragment : Fragment(R.layout.fragement_mypage) {

    private var binding: FragementMypageBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragementMypageBinding = FragementMypageBinding.bind(view)
        binding = fragementMypageBinding

        initLayout()
        initSignInOutButton()
        initSignUpButton()
        initButtonsEnable()
    }

    private fun initButtonsEnable() {
        if(checkIsSignIn()){
            binding?.singUpButton?.isEnabled =false
            binding?.signInOutButton?.isEnabled =true
        }
        binding?.let { binding->
            binding.emailEditText.addTextChangedListener {
                if(checkIsSignIn().not() && binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()){
                    binding.signInOutButton.isEnabled=true
                    binding.singUpButton.isEnabled=true
                }
            }
            binding.passwordEditText.addTextChangedListener {
                if(checkIsSignIn().not() && binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()){
                    binding.signInOutButton.isEnabled=true
                    binding.singUpButton.isEnabled=true
                }
            }
        }
    }

    private fun initSignUpButton() {
        binding?.let { binding->
            binding.singUpButton.setOnClickListener {
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            Toast.makeText(this.context, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this.context, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun initLayout() {
        if (checkIsSignIn()) {
            readyToSignOut()
        } else {
            readyToSignIn()
        }
    }

    private fun initSignInOutButton() {
        binding?.let { binding ->
            binding.signInOutButton.setOnClickListener {
                if (checkIsSignIn()) {
                    signOut()
                } else {
                    signIn(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString())
                }
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    readyToSignOut()
                } else {
                    Toast.makeText(this.context, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
                initLayout()
            }
    }

    private fun signOut() {
        if(checkIsSignIn()){
            auth.signOut()
            initLayout()
        }
    }

    private fun checkIsSignIn(): Boolean {
        return auth.currentUser != null
    }

    private fun readyToSignIn() {
        binding?.let { binding ->
            binding.emailEditText.isEnabled = true
            binding.passwordEditText.isEnabled = true
            binding.signInOutButton.text = "로그인"
        }
    }

    private fun readyToSignOut() {
        binding?.let { binding ->
            binding.singUpButton.isEnabled = false
            binding.emailEditText.setText(auth.currentUser?.email.orEmpty())
            binding.passwordEditText.setText("********")
            binding.emailEditText.isEnabled = false
            binding.passwordEditText.isEnabled = false
            binding.signInOutButton.text = "로그아웃"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}