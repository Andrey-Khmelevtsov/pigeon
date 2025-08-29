package com.andrey.hybridchat.models

// data class - это специальный тип класса в Kotlin, идеально подходящий для хранения данных.
// Пустые значения по умолчанию (например, uid = "") нужны для корректной работы Firestore.
data class User(
    val uid: String = "",
    val username: String = "", // Мы пока не используем имя, но заложим на будущее
    val email: String = ""
)