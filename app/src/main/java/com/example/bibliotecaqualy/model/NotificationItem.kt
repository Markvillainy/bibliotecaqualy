package com.example.bibliotecaqualy.model

data class NotificationItem(
    val id: String = "",
    val userId: String = "",       // UID del usuario que RECIBE la notificación
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)