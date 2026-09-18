package com.example.bibliotecaqualy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PerfilScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: "Usuario"

    Column(
        modifier = Modifier.fillMaxSize().background(QualyBackground).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Perfil de Usuario", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = userEmail, style = MaterialTheme.typography.titleMedium, color = Color.DarkGray)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC82828))
        ) {
            Text("Cerrar Sesión", color = Color.White)
        }
    }
}