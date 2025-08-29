package com.andrey.hybridchat.models

data class Message(
    val text: String? = null,
    val senderId: String? = null,
    val timestamp: Long = 0L,
    val channel: String = "firebase" // <-- НОВОЕ ПОЛЕ. По умолчанию "firebase"
)