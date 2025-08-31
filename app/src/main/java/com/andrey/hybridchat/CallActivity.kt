package com.andrey.hybridchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView

class CallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        val channelId = intent.getStringExtra("CHANNEL_ID")
        val userName = intent.getStringExtra("USER_NAME")

        val callStatusTextView: TextView = findViewById(R.id.callStatusTextView)
        val hangUpButton: ImageButton = findViewById(R.id.hangUpButton)

        // Отображаем имя собеседника, которому звоним
        callStatusTextView.text = "Звонок для\n$userName"

        hangUpButton.setOnClickListener {
            // TODO: Добавить логику завершения звонка
            finish() // Пока просто закрываем экран
        }

        // TODO: Добавить логику инициализации Agora и начала звонка
    }
}