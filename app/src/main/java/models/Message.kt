package com.andrey.hybridchat.models

data class Message(
    val text: String? = null,
    val senderId: String? = null,
    val timestamp: Long = 0L,
    val channel: String = "firebase",
    val attachmentUrl: String? = null,
    val attachmentType: String? = null
)