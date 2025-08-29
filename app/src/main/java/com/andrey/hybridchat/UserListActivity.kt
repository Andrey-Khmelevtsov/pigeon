package com.andrey.hybridchat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserListActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    // Код-запрос для разрешений, может быть любым числом
    private val SMS_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        val userRecyclerView: RecyclerView = findViewById(R.id.userRecyclerView)
        userAdapter = UserAdapter(userList)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter

        // Проверяем и запрашиваем разрешения
        checkAndRequestSmsPermissions()
    }

    private fun checkAndRequestSmsPermissions() {
        // Проверяем, есть ли у нас уже нужные разрешения
        val sendSmsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        val receiveSmsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)

        // Если хотя бы одного разрешения нет
        if (sendSmsPermission != PackageManager.PERMISSION_GRANTED || receiveSmsPermission != PackageManager.PERMISSION_GRANTED) {
            // Показываем системное диалоговое окно с запросом
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else {
            // Если разрешения уже есть, просто загружаем пользователей
            fetchUsers()
        }
    }

    // Этот метод вызывается после того, как пользователь ответил на запрос разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            // Если пользователь дал разрешения (или хотя бы одно из них), загружаем список
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUsers()
            } else {
                // Можно показать сообщение, что без разрешений SMS-режим работать не будет
                // Но пока просто загружаем пользователей в любом случае
                fetchUsers()
            }
        }
    }

    private fun fetchUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    if (user.uid != auth.currentUser?.uid) {
                        userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
                Log.d("UserListActivity", "Users loaded: ${userList.size}")
            }
            .addOnFailureListener { exception ->
                Log.w("UserListActivity", "Error getting documents: ", exception)
            }
    }
}