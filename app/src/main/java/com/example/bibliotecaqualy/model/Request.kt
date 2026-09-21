package com.example.bibliotecaqualy.model

data class Request(
    val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val status: String = "PENDIENTE" // PENDIENTE, ACEPTADA, RECHAZADA
)