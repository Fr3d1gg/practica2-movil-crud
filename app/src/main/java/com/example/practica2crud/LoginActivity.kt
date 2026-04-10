package com.example.practica2crud

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practica2crud.model.LoginRequest
import com.example.practica2crud.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsernameLogin: EditText
    private lateinit var etPasswordLogin: EditText
    private lateinit var btnLoginSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsernameLogin = findViewById(R.id.etUsernameLogin)
        etPasswordLogin = findViewById(R.id.etPasswordLogin)
        btnLoginSubmit = findViewById(R.id.btnLoginSubmit)

        btnLoginSubmit.setOnClickListener {
            iniciarSesion()
        }
    }

    private fun iniciarSesion() {
        val username = etUsernameLogin.text.toString().trim()
        val password = etPasswordLogin.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(username, password)
                )

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    val token = loginResponse.access_token
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(
                            this@LoginActivity,
                            "No se recibió token",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("jwt_token", token)
                        .putInt("user_id", loginResponse.user_id ?: -1)
                        .putString("username", loginResponse.username ?: "")
                        .apply()

                    Toast.makeText(
                        this@LoginActivity,
                        loginResponse.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@LoginActivity, CrudActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Credenciales inválidas",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}