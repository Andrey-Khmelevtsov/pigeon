package com.andrey.hybridchat

import android.content.Intent // <--- ВОТ СЮДА
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

        // Получаем данные, которые мы отправляли из нашей Cloud Function
        val data = message.data
        val type = data["type"]

        Log.d("FCM_MESSAGE", "Получено сообщение типа: $type с данными: $data")

        // Проверяем, что это именно уведомление о входящем звонке
        if (type == "incoming_call") {
            val senderName = data["sender_name"]
            val channelName = data["channel_name"]

            if (senderName != null && channelName != null) {
                // Создаем намерение (Intent) открыть экран входящего звонка
                val intent = Intent(this, CallActivity::class.java).apply {
                    // Очень важно! Этот флаг нужен, чтобы запустить Activity из сервиса,
                    // который работает в фоне.
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    // Передаем на экран звонка все необходимые данные
                    putExtra("IS_INCOMING_CALL", true) // Флаг, что это входящий, а не исходящий
                    putExtra("USER_NAME", senderName)
                    putExtra("CHANNEL_ID", channelName)
                }
                startActivity(intent)
            }
        }
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