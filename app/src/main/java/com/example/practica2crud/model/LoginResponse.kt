package com.example.practica2crud.model

data class LoginResponse(
    val status: String,
    val message: String,
    val user_id: Int?,
    val username: String?,
    val access_token: String?
)