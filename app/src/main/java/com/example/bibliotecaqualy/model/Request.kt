package com.example.bibliotecaqualy.model

data class Request(
    val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val applicantId: String = "",
    val requesterName: String = "", // O applicantName
    val type: String = "Préstamo", // "Préstamo" o "Intercambio"
    val status: String = "PENDIENTE", // "PENDIENTE", "ACEPTADA", "RECHAZADA"
    val timestamp: Long = System.currentTimeMillis()
)