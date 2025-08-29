package com.andrey.hybridchat.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.andrey.hybridchat.models.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SmsReceiver : BroadcastReceiver() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val senderNum = sms.originatingAddress
                val messageText = sms.messageBody

                if (senderNum != null) {
                    // Если номер отправителя есть, ищем его в нашей базе
                    findUserByPhoneNumber(senderNum, messageText)
                }
            }
        }
    }

    private fun findUserByPhoneNumber(phoneNumber: String, messageText: String) {
        // Ищем в коллекции "users" документ, где поле "phoneNumber" равно номеру отправителя
        db.collection("users")
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Пользователь найден!
                    val senderUser = documents.first() // Берем первого найденного
                    val senderId = senderUser.getString("uid")
                    val ourUserId = auth.currentUser?.uid

                    if (senderId != null && ourUserId != null) {
                        saveSmsToChat(senderId, ourUserId, messageText)
                    }
                } else {
                    Log.d("SmsReceiver", "No user found with phone number: $phoneNumber")
                }
            }
    }

    private fun saveSmsToChat(senderId: String, receiverId: String, messageText: String) {
        // Создаем ID комнаты чата (точно так же, как в ChatActivity)
        val chatRoomId = listOf(senderId, receiverId).sorted().joinToString("_")

        // Создаем объект сообщения
        val message = Message(
            text = messageText,
            senderId = senderId,
            timestamp = System.currentTimeMillis()
        )

        // Сохраняем сообщение в нужный чат в Firestore
        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("SmsReceiver", "SMS from $senderId saved to chat $chatRoomId")
            }
    }
}