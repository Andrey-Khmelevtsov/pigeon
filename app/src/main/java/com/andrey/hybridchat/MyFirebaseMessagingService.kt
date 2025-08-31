package com.andrey.hybridchat

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Новый токен получен: $token")
        // Вызываем нашу новую функцию для сохранения токена
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM_MESSAGE", "Сообщение получено!")
        Log.d("FCM_MESSAGE", "Данные: ${message.data}")
        // TODO: Здесь будет основная логика обработки входящего звонка.
    }

    // 👇 ФУНКЦИЯ, КОТОРУЮ Я ДОБАВИЛ 👇
    private fun saveTokenToFirestore(token: String) {
        // Получаем ID текущего пользователя. Если никто не вошел, выходим.
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Создаем ссылку на документ пользователя в коллекции "users"
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        // Создаем объект с полем для токена
        val tokenData = hashMapOf(
            "fcmToken" to token
        )

        // Обновляем документ, добавляя или перезаписывая поле fcmToken
        userDocRef.set(tokenData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FCM_TOKEN", "Токен успешно сохранен в Firestore для пользователя $userId")
            }
            .addOnFailureListener { e ->
                Log.e("FCM_TOKEN", "Ошибка при сохранении токена в Firestore", e)
            }
    }
}