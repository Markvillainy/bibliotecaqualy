package com.example.bibliotecaqualy.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

// Modelo de datos para las tarjetas de Actividad Reciente
data class ActivityItem(
    val title: String,
    val description: String,
    val timeAgo: String,
    val timestamp: Long
)

class BookViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _books = mutableStateListOf<Book>()
    val books: List<Book> get() = _books

    // Lista de solicitudes en tiempo real para el Buzón
    private val _requests = mutableStateListOf<Request>()
    val requests: List<Request> get() = _requests

    init {
        listenToBookUpdates()
        listenToRequestUpdates()
    }

    private fun listenToBookUpdates() {
        db.collection("books")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookViewModel", "Error al consultar libros en Firestore", error)
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
                }
            }
    }

    private fun listenToRequestUpdates() {
        db.collection("requests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookViewModel", "Error al consultar solicitudes en Firestore", error)
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
                            Log.e("BookViewModel", "Error deserializando solicitud ${doc.id}", e)
                        }
                    }
                }
            }
    }

    // --- FUNCIONES PARA PERFIL Y ESTADÍSTICAS ---

    // 1. Contador de Préstamos Aceptados
    fun getActiveLoansCount(currentUserId: String): Int {
        return _requests.count { request ->
            (request.applicantId == currentUserId || request.ownerId == currentUserId) &&
                    request.status.equals("ACEPTADA", ignoreCase = true)
        }
    }

    // 2. Generador de Lista de Actividad Reciente (Publicaciones y Préstamos Aceptados)
    fun getRecentActivity(currentUserId: String): List<ActivityItem> {
        val activityList = mutableListOf<ActivityItem>()

        // Libros publicados por el usuario
        _books.filter { it.ownerId == currentUserId }.forEach { book ->
            activityList.add(
                ActivityItem(
                    title = "Libro publicado",
                    description = "${book.title} - ${book.author}",
                    timeAgo = "Reciente",
                    timestamp = System.currentTimeMillis() // Puedes mapear book.timestamp si existe en tu modelo Book
                )
            )
        }

        // Solicitudes del usuario que han sido ACEPTADAS
        _requests.filter {
            (it.applicantId == currentUserId || it.ownerId == currentUserId) &&
                    it.status.equals("ACEPTADA", ignoreCase = true)
        }.forEach { req ->
            val otherPerson = if (req.ownerId == currentUserId) req.requesterName else req.ownerName
            activityList.add(
                ActivityItem(
                    title = "Préstamo registrado",
                    description = "${req.bookTitle} a $otherPerson",
                    timeAgo = "Reciente",
                    timestamp = req.timestamp
                )
            )
        }

        // Ordenar por más reciente primero
        return activityList.sortedByDescending { it.timestamp }
    }

    // --- OPERACIONES EN FIRESTORE ---

    fun addBook(book: Book, onSuccess: () -> Unit = {}) {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid ?: ""
        val currentUserEmail = currentUser?.email ?: "Usuario"

        val newBook = book.copy(
            ownerId = currentUserId,
            ownerName = currentUserEmail
        )

        db.collection("books").document(newBook.id).set(newBook)
            .addOnSuccessListener {
                Log.d("BookViewModel", "Libro publicado con éxito: ${newBook.id}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("BookViewModel", "Error al guardar el libro", e)
            }
    }

    fun updateBook(updatedBook: Book, onSuccess: () -> Unit = {}) {
        db.collection("books").document(updatedBook.id).set(updatedBook)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> Log.e("BookViewModel", "Error al actualizar", e) }
    }

    fun deleteBook(bookId: String) {
        db.collection("books").document(bookId).delete()
            .addOnFailureListener { e -> Log.e("BookViewModel", "Error al eliminar", e) }
    }

    // Enviar solicitud desde el detalle del libro
    fun sendRequest(context: Context, book: Book) {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid ?: ""
        val currentUserEmail = currentUser?.email ?: "Estudiante"

        val requestId = UUID.randomUUID().toString()
        val request = Request(
            id = requestId,
            bookId = book.id,
            bookTitle = book.title,
            ownerId = book.ownerId,
            ownerName = book.ownerName,
            applicantId = currentUserId,
            requesterName = currentUserEmail,
            type = "Préstamo",
            status = "PENDIENTE",
            timestamp = System.currentTimeMillis()
        )

        db.collection("requests").document(requestId).set(request)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Solicitud enviada a ${book.ownerName.ifEmpty { "el usuario" }}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Error al enviar la solicitud",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("BookViewModel", "Error al enviar solicitud", e)
            }
    }

    // Aceptar o Rechazar solicitud desde el Buzón
    fun updateRequestStatus(context: Context, requestId: String, newStatus: String) {
        db.collection("requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener {
                val mensaje = if (newStatus == "ACEPTADA") "Solicitud Aceptada" else "Solicitud Rechazada"
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al actualizar el estado", Toast.LENGTH_SHORT).show()
                Log.e("BookViewModel", "Error al actualizar solicitud", e)
            }
    }
}