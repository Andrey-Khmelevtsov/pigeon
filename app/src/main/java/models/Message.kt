package com.andrey.hybridchat.models

// "Чертеж" для одного сообщения в чате
data class Message(
    val text: String? = null,
    val senderId: String? = null,
    val timestamp: Long = 0L
)