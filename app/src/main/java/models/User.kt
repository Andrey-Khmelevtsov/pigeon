package com.andrey.hybridchat.models

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String? = null // <-- НОВОЕ ПОЛЕ
)