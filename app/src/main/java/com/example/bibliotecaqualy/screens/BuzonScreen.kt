package com.example.bibliotecaqualy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BuzonScreen(viewModel: BookViewModel) {
    val context = LocalContext.current

    // 1. Recolectar el StateFlow para que Compose redibuje en tiempo real
    val requests by viewModel.requests.collectAsState()

    // 2. Obtener el usuario activo actual
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    // 3. Filtrar solicitudes PENDIENTES sobre la lista recolectada
    val pendingRequests = requests.filter { req ->
        req.status == "PENDIENTE" && (
                (currentUserId.isNotEmpty() && req.ownerId == currentUserId) ||
                        (currentUserEmail.isNotEmpty() && req.ownerName.equals(currentUserEmail, ignoreCase = true))
                )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Buzón de Solicitudes",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes solicitudes pendientes.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(pendingRequests) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = req.requesterName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Solicita: ${req.bookTitle}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.updateRequestStatus(context, req.id, "ACEPTADA")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = QualyGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Aceptar")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateRequestStatus(context, req.id, "RECHAZADA")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Rechazar", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}