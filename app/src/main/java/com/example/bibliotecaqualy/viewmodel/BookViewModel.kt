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
        val newBook = book.copy(
            ownerId = currentUserId,
            ownerName = currentUserEmail
        )

        db.collection("books").document(newBook.id).set(newBook)
            .addOnSuccessListener {
                Log.d("BookViewModel", "Libro guardado exitosamente: ${newBook.id}")
            }
            .addOnFailureListener { e ->
                Log.e("BookViewModel", "Error al guardar el libro", e)
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
}