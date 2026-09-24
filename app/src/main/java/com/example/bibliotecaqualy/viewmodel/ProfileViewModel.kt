package com.example.bibliotecaqualy.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bibliotecaqualy.model.ActivityLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var userName by mutableStateOf("")
        private set
    var userEmail by mutableStateOf("")
        private set
    var userCareer by mutableStateOf("Estudiante Universitario")
        private set

    var publishedCount by mutableIntStateOf(0)
        private set
    var exchangesCount by mutableIntStateOf(0)
        private set
    var loansCount by mutableIntStateOf(0)
        private set

    private val _activities = mutableStateListOf<ActivityLog>()
    val activities: List<ActivityLog> get() = _activities

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val user = auth.currentUser ?: return
        userEmail = user.email ?: ""
        userName = user.displayName?.ifEmpty { userEmail.substringBefore("@") } ?: userEmail.substringBefore("@")

        // 1. Obtener datos extra del usuario desde Firestore (si guardaste carrera/universidad)
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    val career = document.getString("career")
                    if (!name.isNullOrEmpty()) userName = name
                    if (!career.isNullOrEmpty()) userCareer = career
                }
            }

        // 2. Contar libros publicados por este usuario en tiempo real
        db.collection("books")
            .whereEqualTo("ownerId", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                publishedCount = snapshot.size()
            }

        // 3. Contar intercambios y préstamos aceptados
        db.collection("requests")
            .whereEqualTo("requesterId", user.uid)
            .whereEqualTo("status", "Aceptada")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                exchangesCount = snapshot.size()
            }

        // 4. Obtener actividad reciente ordenada por fecha
        db.collection("activities")
            .whereEqualTo("userId", user.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (snapshot.metadata.hasPendingWrites()) return@addSnapshotListener

                _activities.clear()
                for (doc in snapshot.documents) {
                    val activity = doc.toObject(ActivityLog::class.java)
                    if (activity != null) {
                        _activities.add(activity)
                    }
                }
            }
    }

    // Método auxiliar para registrar actividades desde cualquier parte de la app
    fun logActivity(title: String, detail: String) {
        val userId = auth.currentUser?.uid ?: return
        val newActivity = ActivityLog(
            userId = userId,
            title = title,
            detail = detail,
            timestamp = com.google.firebase.Timestamp.now()
        )
        db.collection("activities").add(newActivity)
    }
}