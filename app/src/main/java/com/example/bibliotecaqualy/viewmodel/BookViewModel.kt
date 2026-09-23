package com.example.bibliotecaqualy.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.model.Request
import com.example.bibliotecaqualy.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class BookViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Lista en memoria para los libros
    private val _books = mutableStateListOf<Book>()
    val books: List<Book> get() = _books

    // Lista en memoria para las solicitudes
    private val _requests = mutableStateListOf<Request>()
    val requests: List<Request> get() = _requests

    init {
        listenToBookUpdates()
        listenToRequestUpdates() // <--- Escucha activa de solicitudes agregada
    }

    // Escuchar cambios en la colección de Libros
    private fun listenToBookUpdates() {
        db.collection("books")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _books.clear()
                for (doc in snapshot.documents) {
                    val book = doc.toObject(Book::class.java)
                    if (book != null) {
                        _books.add(book)
                    }
                }
            }
    }

    // --- BLOQUE DE SNAPSHOTS PARA SOLICITUDES EN TIEMPO REAL ---
    private fun listenToRequestUpdates() {
        db.collection("requests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookViewModel", "Error al escuchar las solicitudes", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _requests.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val request = doc.toObject(Request::class.java)
                            if (request != null) {
                                _requests.add(request)
                            }
                        } catch (e: Exception) {
                            Log.e("BookViewModel", "Error al parsear solicitud ${doc.id}", e)
                        }
                    }
                }
            }
    }

    // --- FUNCIONES CRUD Y ACCIONES ---

    fun addBook(book: Book) {
        val currentUserId = auth.currentUser?.uid ?: ""
        val currentUserEmail = auth.currentUser?.email ?: "Usuario"
        val newBook = book.copy(ownerId = currentUserId, ownerName = currentUserEmail)

        db.collection("books").document(newBook.id).set(newBook)
    }

    fun updateBook(updatedBook: Book) {
        db.collection("books").document(updatedBook.id).set(updatedBook)
    }

    fun deleteBook(bookId: String) {
        db.collection("books").document(bookId).delete()
    }

    fun sendRequest(context: Context, book: Book) {
        val currentUser = auth.currentUser
        val requesterId = currentUser?.uid ?: ""
        val requesterName = currentUser?.email ?: "Usuario Estudiante"

        val newRequest = Request(
            id = UUID.randomUUID().toString(),
            bookId = book.id,
            bookTitle = book.title,
            requesterId = requesterId,
            requesterName = requesterName,
            ownerId = book.ownerId,
            ownerName = book.ownerName,
            status = "PENDIENTE"
        )

        // Guardar en la colección "requests" de Firestore
        db.collection("requests").document(newRequest.id).set(newRequest)

        // Notificación local
        NotificationHelper.showNotification(
            context = context,
            title = "¡Nueva solicitud enviada!",
            message = "Has solicitado el libro '${book.title}' a ${book.ownerName}"
        )
    }

    fun updateRequestStatus(context: Context, requestId: String, newStatus: String) {
        // Actualizar en Firestore
        db.collection("requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener {
                val mensajeStatus = if (newStatus == "ACEPTADA") "aceptada" else "rechazada"
                NotificationHelper.showNotification(
                    context = context,
                    title = "Solicitud $mensajeStatus",
                    message = "La solicitud fue $mensajeStatus con éxito."
                )
            }
    }
}