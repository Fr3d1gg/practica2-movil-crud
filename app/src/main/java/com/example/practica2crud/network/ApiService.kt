package com.example.practica2crud.network

import com.example.practica2crud.model.LoginRequest
import com.example.practica2crud.model.LoginResponse
import com.example.practica2crud.model.MessageResponse
import com.example.practica2crud.model.Note
import com.example.practica2crud.model.NoteRequest
import com.example.practica2crud.model.RegisterRequest
import com.example.practica2crud.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("notes")
    suspend fun createNote(
        @Header("Authorization") token: String,
        @Body note: NoteRequest
    ): Response<Map<String, Any>>

    @GET("notes")
    suspend fun getNotes(
        @Header("Authorization") token: String
    ): Response<List<Note>>

    @PUT("notes/{note_id}")
    suspend fun updateNote(
        @Header("Authorization") token: String,
        @Path("note_id") noteId: Int,
        @Body note: NoteRequest
    ): Response<Map<String, Any>>

    @DELETE("notes/{note_id}")
    suspend fun deleteNote(
        @Header("Authorization") token: String,
        @Path("note_id") noteId: Int
    ): Response<MessageResponse>
}