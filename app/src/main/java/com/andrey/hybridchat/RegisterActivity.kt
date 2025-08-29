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

        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val registerButton: Button = findViewById(R.id.buttonRegister)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите e-mail и пароль.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("RegisterActivity", "createUserWithEmail:success")

                        // ----- НОВЫЙ КОД ЗДЕСЬ -----
                        // После успешной регистрации в Auth, создаем запись в Firestore
                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser?.uid

                        if (userId != null) {
                            val user = User(uid = userId, email = email)
                            db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener { Log.d("RegisterActivity", "User profile created in Firestore") }
                                .addOnFailureListener { e -> Log.w("RegisterActivity", "Error creating user profile", e) }
                        }
                        // ----- КОНЕЦ НОВОГО КОДА -----

                        Toast.makeText(baseContext, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Ошибка регистрации: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}