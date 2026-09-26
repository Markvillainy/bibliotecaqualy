package com.example.bibliotecaqualy.model

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "Literatura",
    val edition: String = "",
    val state: String = "Excelente",
    val description: String = "",
    val coverUrl: String = "", // Guarda la URL pública de la imagen
    val ownerId: String = "",
    val ownerName: String = "Usuario",
    val availabilityStatus: String = "Disponible"
)