package com.andrey.hybridchat

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        // Находим элементы интерфейса
        val emailEditText: EditText = findViewById(R.id.editTextEmailLogin)
        val passwordEditText: EditText = findViewById(R.id.editTextPasswordLogin)
        val loginButton: Button = findViewById(R.id.buttonLogin)

        // Устанавливаем слушателя на кнопку "Войти"
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите e-mail и пароль.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ГЛАВНЫЙ МЕТОД ДЛЯ ВХОДА
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Успех
                        Log.d("LoginActivity", "signInWithEmail:success")
                        Toast.makeText(baseContext, "Вход успешен.", Toast.LENGTH_SHORT).show()
                        // В будущем здесь будет переход на экран со списком чатов
                    } else {
                        // Провал
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Ошибка входа: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}