package com.example.bibliotecaqualy.model

import com.google.firebase.Timestamp

data class ActivityLog(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val detail: String = "",
    val timestamp: Timestamp = Timestamp.now()
)