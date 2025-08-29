package com.andrey.hybridchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Находим нашу новую панель в макете
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        // Назначаем ее главной панелью для этого экрана
        setSupportActionBar(toolbar)

        val intent = intent
        val receiverUid = intent.getStringExtra("USER_UID")
        val receiverEmail = intent.getStringExtra("USER_EMAIL")

        // Теперь эта команда будет работать с НАШЕЙ панелью и гарантированно установит заголовок
        supportActionBar?.title = receiverEmail
    }
}