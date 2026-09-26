package com.example.bibliotecaqualy.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class ActivityItem(
    val title: String,
    val description: String,
    val timeAgo: String,
    val timestamp: Long
)

class BookViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Manejo de estado reactivo mediante StateFlow
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests.asStateFlow()

    private var booksListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null

    init {
        listenToUpdates()
    }

    fun listenToUpdates() {
        booksListener?.remove()
        requestsListener?.remove()

        booksListener = db.collection("books")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookViewModel", "Error al consultar libros", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookList = mutableListOf<Book>()
                    for (doc in snapshot.documents) {
                        try {
                            doc.toObject(Book::class.java)?.let { bookList.add(it) }
                        } catch (e: Exception) {
                            Log.e("BookViewModel", "Error deserializando libro", e)
                        }
                    }
                    _books.value = bookList
                }
            }

        requestsListener = db.collection("requests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookViewModel", "Error al consultar solicitudes", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requestList = mutableListOf<Request>()
                    for (doc in snapshot.documents) {
                        try {
                            doc.toObject(Request::class.java)?.let { requestList.add(it) }
                        } catch (e: Exception) {
                            Log.e("BookViewModel", "Error deserializando solicitud", e)
                        }
                    }
                    _requests.value = requestList
                }
            }
    }

    // --- AUTENTICACIÓN CON GOOGLE ---

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                listenToUpdates() // Reinicia listeners para el usuario autenticado
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Error al autenticar con Google")
            }
    }

    // --- MÉTODOS DE PERFIL Y ACTIVIDAD ---

    fun getActiveLoansCount(currentUserId: String? = null): Int {
        val targetUid = currentUserId ?: auth.currentUser?.uid ?: return 0
        return _requests.value.count { request ->
            (request.applicantId == targetUid || request.ownerId == targetUid) &&
                    request.status.equals("ACEPTADA", ignoreCase = true)
        }
    }

    fun getRecentActivity(currentUserId: String? = null): List<ActivityItem> {
        val targetUid = currentUserId ?: auth.currentUser?.uid ?: return emptyList()
        val activityList = mutableListOf<ActivityItem>()

        _books.value.filter { it.ownerId == targetUid }.forEach { book ->
            activityList.add(
                ActivityItem(
                    title = "Libro publicado",
                    description = "${book.title} - ${book.author}",
                    timeAgo = "Reciente",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        _requests.value.filter {
            (it.applicantId == targetUid || it.ownerId == targetUid) &&
                    it.status.equals("ACEPTADA", ignoreCase = true)
        }.forEach { req ->
            val otherPerson = if (req.ownerId == targetUid) req.requesterName else req.ownerName
            activityList.add(
                ActivityItem(
                    title = "Préstamo registrado",
                    description = "${req.bookTitle} a $otherPerson",
                    timeAgo = "Reciente",
                    timestamp = req.timestamp
                )
            )
        }

        return activityList.sortedByDescending { it.timestamp }
    }

    // --- OPERACIONES DE FIRESTORE ---

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

    override fun onCleared() {
        super.onCleared()
        booksListener?.remove()
        requestsListener?.remove()
    }
}