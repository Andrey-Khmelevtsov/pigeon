package com.andrey.hybridchat.models

data class Message(
    val text: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null, // <--- ДОБАВИЛ ЭТУ СТРОКУ
    val timestamp: Long = 0L,
    val channel: String = "firebase",
    val attachmentUrl: String? = null,
    val attachmentType: String? = null
) {
    // 👇 ДОБАВИЛ ЭТОТ БЛОК. ОН ОБЯЗАТЕЛЕН ДЛЯ FIREBASE
    constructor() : this(null, null, null, 0L, "firebase", null, null)
}