package com.example.practica2crud

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practica2crud.model.Note
import com.example.practica2crud.model.NoteRequest
import com.example.practica2crud.network.RetrofitClient
import kotlinx.coroutines.launch

class CrudActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var listViewNotes: ListView

    private var selectedNoteId: Int? = null
    private var notesList: List<Note> = emptyList()
    private var jwtToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crud)

        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnSave = findViewById(R.id.btnSave)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        listViewNotes = findViewById(R.id.listViewNotes)

        jwtToken = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("jwt_token", null)

        if (jwtToken.isNullOrEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnSave.setOnClickListener { createNote() }
        btnUpdate.setOnClickListener { updateNote() }
        btnDelete.setOnClickListener { deleteNote() }

        listViewNotes.setOnItemClickListener { _, _, position, _ ->
            val note = notesList[position]
            selectedNoteId = note.id
            etTitle.setText(note.title)
            etContent.setText(note.content)
        }

        loadNotes()
    }

    private fun authHeader(): String = "Bearer $jwtToken"

    private fun createNote() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Completa título y contenido", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createNote(
                    authHeader(),
                    NoteRequest(title, content)
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@CrudActivity, "Nota creada", Toast.LENGTH_SHORT).show()
                    clearFields()
                    loadNotes()
                } else {
                    Toast.makeText(this@CrudActivity, "No se pudo crear la nota", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrudActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getNotes(authHeader())

                if (response.isSuccessful && response.body() != null) {
                    notesList = response.body()!!

                    val items = notesList.map { note ->
                        "ID: ${note.id}\nTítulo: ${note.title}\nContenido: ${note.content}"
                    }

                    val adapter = ArrayAdapter(
                        this@CrudActivity,
                        android.R.layout.simple_list_item_1,
                        items
                    )

                    listViewNotes.adapter = adapter
                } else {
                    Toast.makeText(this@CrudActivity, "No se pudieron cargar las notas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrudActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateNote() {
        val noteId = selectedNoteId
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (noteId == null) {
            Toast.makeText(this, "Selecciona una nota", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Completa título y contenido", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateNote(
                    authHeader(),
                    noteId,
                    NoteRequest(title, content)
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@CrudActivity, "Nota actualizada", Toast.LENGTH_SHORT).show()
                    clearFields()
                    loadNotes()
                } else {
                    Toast.makeText(this@CrudActivity, "No se pudo actualizar la nota", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrudActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteNote() {
        val noteId = selectedNoteId

        if (noteId == null) {
            Toast.makeText(this, "Selecciona una nota", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteNote(
                    authHeader(),
                    noteId
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@CrudActivity, "Nota eliminada", Toast.LENGTH_SHORT).show()
                    clearFields()
                    loadNotes()
                } else {
                    Toast.makeText(this@CrudActivity, "No se pudo eliminar la nota", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrudActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun clearFields() {
        etTitle.text.clear()
        etContent.text.clear()
        selectedNoteId = null
    }
}