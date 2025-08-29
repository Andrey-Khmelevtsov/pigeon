package com.andrey.hybridchat

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.andrey.hybridchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        // Находим все четыре поля
        val usernameEditText: EditText = findViewById(R.id.editTextUsername) // <-- НОВОЕ
        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val phoneEditText: EditText = findViewById(R.id.editTextPhone)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val registerButton: Button = findViewById(R.id.buttonRegister)

        registerButton.setOnClickListener {
            // Считываем данные из всех полей
            val username = usernameEditText.text.toString() // <-- НОВОЕ
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Добавляем проверку для имени
            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) { // <-- ИЗМЕНЕНО
                Toast.makeText(this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("RegisterActivity", "createUserWithEmail:success")

                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser?.uid

                        if (userId != null) {
                            // При создании объекта User теперь передаем и имя
                            val user = User(uid = userId, username = username, email = email, phoneNumber = phone) // <-- ИЗМЕНЕНО
                            db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener { Log.d("RegisterActivity", "User profile created in Firestore") }
                                .addOnFailureListener { e -> Log.w("RegisterActivity", "Error creating user profile", e) }
                        }

                        Toast.makeText(baseContext, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Ошибка регистрации: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}