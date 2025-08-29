package com.andrey.hybridchat.models

data class User(
    val uid: String = "",
    val username: String = "", // <-- МЫ УЖЕ ДОБАВЛЯЛИ ЕГО РАНЬШЕ, ТАК ЧТО ОН ГОТОВ
    val email: String = "",
    val phoneNumber: String? = null
)