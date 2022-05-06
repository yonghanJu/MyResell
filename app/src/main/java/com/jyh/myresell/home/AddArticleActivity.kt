package com.jyh.myresell.home

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.jyh.myresell.DBKey.Companion.DB_ARTICLES
import com.jyh.myresell.databinding.ActivityAddArticleBinding
import com.jyh.myresell.model.ArticleModel

class AddArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddArticleBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var selectedUri: Uri? =null
    private val storage by lazy{
        Firebase.storage
    }
    private val auth by lazy{
        Firebase.auth
    }
    private val articleDB by lazy{
        Firebase.database.reference.child(DB_ARTICLES)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initActivityResult()
        initFABButton()
        initSubmitButton()
    }

    private fun initSubmitButton() {
        binding.submitButton.setOnClickListener {
            showProgress()
            val title = binding.titleEditText.text.toString()
            val price = binding.priceEditText.text.toString()
            Log.d("initSubmitButton", "$title $price")
            val sellerId = auth.currentUser?.uid.orEmpty()
            if(selectedUri != null){
                val photoUri = selectedUri?: return@setOnClickListener
                uploadPhoto(photoUri
                    , successHandler = { uri->
                        uploadArticle(sellerId, title,price, uri)
                    }, errorHandler = {
                        Toast.makeText(this,"사진 업로드 실패",Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            }else{
                uploadArticle(sellerId, title,price, "")
            }
        }
    }

    private fun uploadPhoto(uri:Uri, successHandler: (String)->Unit, errorHandler:()->Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    storage.reference.child("article/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri->
                            successHandler(uri.toString())
                        }.addOnFailureListener {
                            errorHandler()
                        }
                }else{
                    errorHandler()
                }
            }
    }

    private fun uploadArticle(sellerId:String, title:String, price:String, uri: String){
        var model = ArticleModel(sellerId, title, System.currentTimeMillis(), "$price 원", uri)
        // Articles 밑에 임의의 아이탬을 만듬
        articleDB.push()
            .setValue(model) // 임의의 아이탬(key)에 모델이 할당됨
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initFABButton() {
        binding.addPhotoButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                    )
                }
            }
        }
    }

    private fun initActivityResult() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                val uri = it.data?.data
                if (uri != null) {
                    binding.photoImageView.setImageURI(uri)
                    selectedUri = uri
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다")
            .setMessage("사진을 가져오기 위해 필요합니다")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000
                )
            }
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        activityResultLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showProgress(){
        binding.progressBar.visibility = View.VISIBLE
    }
    private fun hideProgress(){
        binding.progressBar.visibility = View.GONE
    }
}