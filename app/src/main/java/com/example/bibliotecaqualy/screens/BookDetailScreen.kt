package com.example.bibliotecaqualy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.util.NotificationHelper
import com.example.bibliotecaqualy.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BookDetailScreen(
    book: Book,
    viewModel: BookViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isMyBook = book.ownerId == currentUserId

    // Estado para bloquear toques múltiples
    var isSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Detalle del Libro",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Portada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = book.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = book.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Autor: ${book.author}", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip(onClick = {}, label = { Text(book.category) })
            SuggestionChip(onClick = {}, label = { Text(book.state) })
            SuggestionChip(onClick = {}, label = { Text("• ${book.availabilityStatus}") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Descripción del ejemplar",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = if (book.description.isNotEmpty()) book.description else "Sin descripción adicional.",
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Validación para evitar autosolicitarse el propio libro
        if (!isMyBook) {
            Button(
                enabled = !isSending, // Se deshabilita mientras envía
                onClick = {
                    if (!isSending) {
                        isSending = true

                        // Una Sola llamada al ViewModel
                        viewModel.sendRequest(context, book)

                        // Notificación flotante para el solicitante
                        NotificationHelper.showNotification(
                            context = context,
                            title = "¡Nueva solicitud enviada!",
                            message = "Has solicitado el libro '${book.title}' a ${book.ownerName}"
                        )

                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QualyGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Solicitar Intercambio / Préstamo", color = Color.White, fontSize = 16.sp)
                }
            }
        } else {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Este libro forma parte de tus publicaciones activas.",
                    color = QualyGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}