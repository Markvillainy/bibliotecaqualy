package com.example.bibliotecaqualy.model

import android.net.Uri
import com.google.firebase.firestore.Exclude

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "Disponibles",
    val genre: String = "Literatura",
    val state: String = "Excelente",
    val edition: String = "",
    val description: String = "",
    val coverUrl: String = "",
    val ownerId: String = "",
    val ownerName: String = "Usuario",
    val availabilityStatus: String = "Disponible"
) {
    // coverUri vive solo en la memoria local para seleccionar la foto desde la galería
    // @get:Exclude le indica a Firestore que NO la guarde para evitar el crash (SIG: 9)
    @get:Exclude
    var coverUri: Uri? = null
}