package com.andrey.hybridchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val emailEditText: EditText = findViewById(R.id.editTextEmailLogin)
        val passwordEditText: EditText = findViewById(R.id.editTextPasswordLogin)
        val loginButton: Button = findViewById(R.id.buttonLogin)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите e-mail и пароль.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginActivity", "signInWithEmail:success")
                        Toast.makeText(baseContext, "Вход успешен.", Toast.LENGTH_SHORT).show()

                        // =======================================================
                        // ============= БЛОК, КОТОРЫЙ Я ДОБАВИЛ ===============
                        // =======================================================
                        Log.d("LOGIN_SUCCESS", "Пользователь успешно вошел. Обновляем FCM токен.")

                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (!tokenTask.isSuccessful) {
                                Log.w("FCM_TOKEN", "Не удалось получить FCM токен.", tokenTask.exception)
                                // Даже если токен не получили, все равно переходим дальше
                                navigateToUserList()
                                return@addOnCompleteListener
                            }

                            // Получаем токен
                            val token = tokenTask.result
                            Log.d("FCM_TOKEN", "Токен принудительно получен: $token")

                            // Сохраняем его в Firestore
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                                userDocRef.set(hashMapOf("fcmToken" to token), SetOptions.merge())
                                    .addOnSuccessListener {
                                        Log.d("FCM_TOKEN", "Токен успешно обновлен в Firestore при входе.")
                                        // Переходим на следующий экран ПОСЛЕ успешного сохранения токена
                                        navigateToUserList()
                                    }
                                    .addOnFailureListener {
                                        Log.e("FCM_TOKEN", "Ошибка при обновлении токена при входе.")
                                        // Переходим дальше, даже если сохранить не удалось
                                        navigateToUserList()
                                    }
                            } else {
                                // Если по какой-то причине нет userId, все равно переходим дальше
                                navigateToUserList()
                            }
                        }
                        // =======================================================
                        // ================= КОНЕЦ БЛОКА ========================
                        // =======================================================

                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Ошибка входа: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun navigateToUserList() {
        val intent = Intent(this, UserListActivity::class.java)
        startActivity(intent)
        finish() // Закрываем экран входа
    }
}