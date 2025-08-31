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

                senderNum?.let {
                    // Если номер отправителя есть, ищем его в нашей базе
                    findUserByPhoneNumber(it, messageText)
                }
            }
        }
    }

    private fun findUserByPhoneNumber(phoneNumber: String, messageText: String) {
        db.collection("users")
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val senderUser = documents.first()
                    val senderId = senderUser.getString("uid")
                    val ourUserId = auth.currentUser?.uid

                    // ИЗМЕНЕНИЕ 1: Заменил if (.. != null) на безопасный и идиоматичный let
                    senderId?.let { safeSenderId ->
                        ourUserId?.let { safeOurUserId ->
                            saveSmsToChat(safeSenderId, safeOurUserId, messageText)
                        }
                    }
                } else {
                    Log.d("SmsReceiver", "Пользователь с номером $phoneNumber не найден в базе.")
                }
            }
    }

    private fun saveSmsToChat(senderId: String, receiverId: String, messageText: String) {
        val chatRoomId = listOf(senderId, receiverId).sorted().joinToString("_")

        // ИЗМЕНЕНИЕ 2: Добавил receiverId в объект Message, это критически важно
        val message = Message(
            text = messageText,
            senderId = senderId,
            receiverId = receiverId, // Это поле было пропущено
            timestamp = System.currentTimeMillis()
        )

        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("SmsReceiver", "SMS от $senderId сохранено в чат $chatRoomId")
            }
    }
}