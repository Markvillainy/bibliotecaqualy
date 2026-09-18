package com.example.bibliotecaqualy.model

import java.util.UUID

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val edition: String = "",
    val state: String = "",
    val availabilityStatus: String = "Disponible",
    val coverUrl: String = "",
    val description: String = "",
    val rating: Double = 5.0,
    val ownerId: String = "",
    val ownerName: String = ""
)