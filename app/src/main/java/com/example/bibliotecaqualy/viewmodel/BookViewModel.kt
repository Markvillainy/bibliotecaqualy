package com.example.bibliotecaqualy.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.model.NotificationItem
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

    // --- NOTIFICACIONES FILTRADAS POR USUARIO ---
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private var booksListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null

    init {
        listenToUpdates()
    }

    fun listenToUpdates() {
        booksListener?.remove()
        requestsListener?.remove()
        notificationsListener?.remove()

        val currentUid = auth.currentUser?.uid ?: ""

        // Escuchar Libros
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

        // Escuchar Solicitudes
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

        // Escuchar Notificaciones SOLO del usuario actual
        if (currentUid.isNotEmpty()) {
            notificationsListener = db.collection("notifications")
                .whereEqualTo("userId", currentUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("BookViewModel", "Error al consultar notificaciones", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val notifList = mutableListOf<NotificationItem>()
                        for (doc in snapshot.documents) {
                            try {
                                doc.toObject(NotificationItem::class.java)?.let { notifList.add(it) }
                            } catch (e: Exception) {
                                Log.e("BookViewModel", "Error deserializando notificación", e)
                            }
                        }
                        _notifications.value = notifList.sortedByDescending { it.timestamp }
                    }
                }
        } else {
            _notifications.value = emptyList()
        }
    }

    // --- FUNCIÓN PARA GUARDAR NOTIFICACIÓN EN FIRESTORE AL USUARIO DESTINO ---
    fun addNotificationToUser(targetUserId: String, title: String, message: String) {
        if (targetUserId.isEmpty()) return

        val notifId = UUID.randomUUID().toString()
        val newNotification = NotificationItem(
            id = notifId,
            userId = targetUserId,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        db.collection("notifications").document(notifId).set(newNotification)
            .addOnFailureListener { e ->
                Log.e("BookViewModel", "Error guardando notificación", e)
            }
    }

    // --- AUTENTICACIÓN CON GOOGLE ---

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                listenToUpdates()
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

    // --- OPERACIONES DE FIRESTORE CON EVENTOS DIRIGIDOS AL USUARIO CORRESPONDIENTE ---

    // 1. Un usuario publica un libro -> Notificación para ÉL MISMO
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
                addNotificationToUser(
                    targetUserId = currentUserId,
                    title = "Libro publicado",
                    message = "Tu libro '${newBook.title}' ya se encuentra disponible en el catálogo."
                )
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

    // 2. Un usuario pide un libro -> Notificación para el DUEÑO DEL LIBRO (quien recibe la solicitud)
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

                // Notificación al DUEÑO del libro
                addNotificationToUser(
                    targetUserId = book.ownerId,
                    title = "Nueva Solicitud",
                    message = "$currentUserEmail ha solicitado tu libro '${book.title}'."
                )

                // Notificación de confirmación al SOLICITANTE
                addNotificationToUser(
                    targetUserId = currentUserId,
                    title = "Solicitud Enviada",
                    message = "Has solicitado el libro '${book.title}' a ${book.ownerName}."
                )
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

    // 3. Se acepta o rechaza -> Notificación para el SOLICITANTE
    fun updateRequestStatus(context: Context, requestId: String, newStatus: String) {
        db.collection("requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener {
                val mensaje = if (newStatus == "ACEPTADA") "Solicitud Aceptada" else "Solicitud Rechazada"
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

                val targetRequest = _requests.value.find { it.id == requestId }
                if (targetRequest != null) {
                    val estadoTexto = if (newStatus == "ACEPTADA") "aceptó" else "rechazó"

                    // Notificación enviada al SOLICITANTE
                    addNotificationToUser(
                        targetUserId = targetRequest.applicantId,
                        title = "Respuesta de Solicitud",
                        message = "${targetRequest.ownerName} $estadoTexto tu solicitud para el libro '${targetRequest.bookTitle}'."
                    )
                }
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
        notificationsListener?.remove()
    }
}