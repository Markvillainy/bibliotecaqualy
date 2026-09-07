package com.example.bibliotecaqualy.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.bibliotecaqualy.model.Book

class BookViewModel : ViewModel() {
    private val _books = mutableStateListOf<Book>()
    val books: List<Book> get() = _books

    init {
        // Libros iniciales para simular los mockups
        _books.addAll(
            listOf(
                Book(
                    title = "Álgebra Lineal",
                    author = "Stanley Grossman",
                    category = "Matemáticas",
                    edition = "7ma Ed.",
                    state = "Excelente",
                    availabilityStatus = "Disponible"
                ),
                Book(
                    title = "El Aleph",
                    author = "Jorge Luis Borges",
                    category = "Literatura",
                    edition = "Edición Especial",
                    state = "Bueno",
                    availabilityStatus = "En préstamo"
                ),
                Book(
                    title = "Química General",
                    author = "Raymond Chang",
                    category = "Ciencias",
                    edition = "10a Ed.",
                    state = "Aceptable",
                    availabilityStatus = "Intercambiado"
                )
            )
        )
    }

    // CREATE / AGREGAR
    fun addBook(book: Book) {
        _books.add(book)
    }

    // UPDATE / EDITAR
    fun updateBook(updatedBook: Book) {
        val index = _books.indexOfFirst { it.id == updatedBook.id }
        if (index != -1) {
            _books[index] = updatedBook
        }
    }

    // DELETE / ELIMINAR (Retirar)
    fun deleteBook(bookId: String) {
        _books.removeAll { it.id == bookId }
    }
}