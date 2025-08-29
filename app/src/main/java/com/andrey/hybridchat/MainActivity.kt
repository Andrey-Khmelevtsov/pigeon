package com.andrey.hybridchat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toLoginButton: Button = findViewById(R.id.toLoginButton)
        val toRegisterButton: Button = findViewById(R.id.toRegisterButton)

        // Слушатель для кнопки "Войти"
        toLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Слушатель для кнопки "Регистрация"
        toRegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}