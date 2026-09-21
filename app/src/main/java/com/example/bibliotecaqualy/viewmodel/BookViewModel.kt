package com.example.bibliotecaqualy.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.bibliotecaqualy.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _books = mutableStateListOf<Book>()
    val books: List<Book> get() = _books

    init {
        listenToBookUpdates()
    }

    private fun listenToBookUpdates() {
        db.collection("books")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookViewModel", "Error al consultar Firestore", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _books.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val book = doc.toObject(Book::class.java)
                            if (book != null) {
                                _books.add(book)
                            }
                        } catch (e: Exception) {
                            Log.e("BookViewModel", "Error deserializando el libro ${doc.id}", e)
                        }
                    }
                    Log.d("BookViewModel", "Total libros cargados: ${_books.size}")
                }
            }
    }

    fun addBook(book: Book) {
        val currentUserId = auth.currentUser?.uid ?: ""
        val currentUserEmail = auth.currentUser?.email ?: "Usuario"

        // Generar un ID único directamente si viene vacío
        val bookId = if (book.id.isEmpty()) db.collection("books").document().id else book.id
        val newBook = book.copy(id = bookId, ownerId = currentUserId, ownerName = currentUserEmail)

        db.collection("books")
            .document(bookId)
            .set(newBook)
            .addOnSuccessListener {
                Log.d("BookViewModel", "Libro guardado con éxito: $bookId")
            }
            .addOnFailureListener { e ->
                Log.e("BookViewModel", "Error al guardar en Firestore", e)
            }
    }

    fun updateBook(updatedBook: Book) {
        db.collection("books").document(updatedBook.id).set(updatedBook)
            .addOnFailureListener { e ->
                Log.e("BookViewModel", "Error al actualizar", e)
            }
    }

    fun deleteBook(bookId: String) {
        db.collection("books").document(bookId).delete()
            .addOnFailureListener { e ->
                Log.e("BookViewModel", "Error al eliminar", e)
            }
    }
    private val _requests = mutableStateListOf<com.example.bibliotecaqualy.model.Request>()
    val requests: List<com.example.bibliotecaqualy.model.Request> get() = _requests

    fun sendRequest(context: android.content.Context, book: Book) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val requesterId = currentUser?.uid ?: "user_test_123"
        val requesterName = currentUser?.displayName ?: currentUser?.email ?: "Usuario Estudiante"

        val newRequest = com.example.bibliotecaqualy.model.Request(
            id = java.util.UUID.randomUUID().toString(),
            bookId = book.id,
            bookTitle = book.title,
            requesterId = requesterId,
            requesterName = requesterName,
            ownerId = book.ownerId,
            ownerName = book.ownerName,
            status = "PENDIENTE"
        )

        _requests.add(newRequest)

        // Notificación simulada al propietario del libro
        com.example.bibliotecaqualy.util.NotificationHelper.showNotification(
            context = context,
            title = "¡Nueva solicitud recibida!",
            message = "$requesterName te ha solicitado el libro '${book.title}'"
        )
    }

    fun updateRequestStatus(context: android.content.Context, requestId: String, newStatus: String) {
        val index = _requests.indexOfFirst { it.id == requestId }
        if (index != -1) {
            val oldReq = _requests[index]
            val updatedReq = oldReq.copy(status = newStatus)
            _requests[index] = updatedReq

            val mensajeStatus = if (newStatus == "ACEPTADA") "aceptada" else "rechazada"

            // Notificación simulada al usuario que solicitó
            com.example.bibliotecaqualy.util.NotificationHelper.showNotification(
                context = context,
                title = "Solicitud $mensajeStatus",
                message = "Tu solicitud para '${oldReq.bookTitle}' fue $mensajeStatus por ${oldReq.ownerName}"
            )
        }
    }
}