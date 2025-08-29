package com.andrey.hybridchat.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {

    // Это главный метод, который система Android вызывает, когда приходит SMS
    override fun onReceive(context: Context, intent: Intent) {
        // Проверяем, что это именно то событие, которое нам нужно (получение SMS)
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            // "Распаковываем" сообщение из "посылки" от системы
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val senderNum = sms.originatingAddress
                val messageText = sms.messageBody

                // Пока что просто показываем всплывающее уведомление
                // Этого достаточно, чтобы проверить, что "слушатель" работает.
                Toast.makeText(
                    context,
                    "Новое SMS от $senderNum: $messageText",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}