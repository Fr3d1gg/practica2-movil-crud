package com.example.practica2crud.model

data class Note(
    val id: Int? = null,
    val title: String,
    val content: String,
    val user_id: Int
)