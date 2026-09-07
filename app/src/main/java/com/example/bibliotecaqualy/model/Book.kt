package com.example.bibliotecaqualy.model
import com.example.bibliotecaqualy.model.Book
import android.net.Uri
import java.util.UUID

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String,
    val category: String, // Matemáticas, Literatura, Ciencias
    val edition: String,
    val state: String, // Excelente, Bueno, Aceptable
    val availabilityStatus: String = "Disponible", // Disponible, En préstamo, Intercambiado
    val coverUri: Uri? = null,
    val description: String = "",
    val rating: Double = 5.0,
    val ownerName: String = "Mateo González"
)