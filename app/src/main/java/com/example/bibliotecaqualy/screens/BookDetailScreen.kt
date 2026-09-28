package com.example.bibliotecaqualy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.util.NotificationHelper
import com.example.bibliotecaqualy.util.swipeGestures
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

    // Tomar el estado (soportando ambas propiedades del modelo)
    val currentStatus = book.availabilityStatus
    val isAvailable = currentStatus.equals("Disponible", ignoreCase = true)

    // Estado para bloquear toques múltiples
    var isSending by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            // Deslizar a la derecha para volver a la pantalla anterior
            .swipeGestures(onSwipeRight = { onBack() })
    ) {
        val isWideScreen = maxWidth > 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Barra superior
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

            if (isWideScreen) {
                // DISPOSICIÓN HORIZONTAL (Tablets / Pantallas Anchas)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Portada a la izquierda
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (book.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = book.coverUrl,
                                contentDescription = book.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = book.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }

                    // Información y botones a la derecha
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(text = book.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Autor: ${book.author}", fontSize = 16.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(onClick = {}, label = { Text(book.category) })
                            SuggestionChip(onClick = {}, label = { Text(book.state) })
                            AvailabilityChip(status = currentStatus)
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

                        // Botón de acción
                        ActionButton(
                            isMyBook = isMyBook,
                            isAvailable = isAvailable,
                            isSending = isSending,
                            onClick = {
                                if (!isSending && isAvailable) {
                                    isSending = true
                                    viewModel.sendRequest(context, book)
                                    NotificationHelper.showNotification(
                                        context = context,
                                        title = "¡Nueva solicitud enviada!",
                                        message = "Has solicitado el libro '${book.title}' a ${book.ownerName}"
                                    )
                                    onBack()
                                }
                            }
                        )
                    }
                }
            } else {
                // DISPOSICIÓN VERTICAL (Celulares en Portrait)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (book.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = book.coverUrl,
                                contentDescription = book.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = book.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(text = book.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Autor: ${book.author}", fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text(book.category) })
                        SuggestionChip(onClick = {}, label = { Text(book.state) })
                        AvailabilityChip(status = currentStatus)
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de acción
                    ActionButton(
                        isMyBook = isMyBook,
                        isAvailable = isAvailable,
                        isSending = isSending,
                        onClick = {
                            if (!isSending && isAvailable) {
                                isSending = true
                                viewModel.sendRequest(context, book)
                                NotificationHelper.showNotification(
                                    context = context,
                                    title = "¡Nueva solicitud enviada!",
                                    message = "Has solicitado el libro '${book.title}' a ${book.ownerName}"
                                )
                                onBack()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailabilityChip(status: String) {
    val isAvailable = status.equals("Disponible", ignoreCase = true)

    val chipContainerColor = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val chipTextColor = if (isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
    val displayText = if (isAvailable) "• Disponible" else "• En Préstamo"

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = displayText,
                color = chipTextColor,
                fontWeight = FontWeight.Bold
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = chipContainerColor
        )
    )
}

@Composable
private fun ActionButton(
    isMyBook: Boolean,
    isAvailable: Boolean,
    isSending: Boolean,
    onClick: () -> Unit
) {
    if (!isMyBook) {
        Button(
            enabled = !isSending && isAvailable,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = QualyGreen,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isAvailable) "Solicitar Intercambio / Préstamo" else "No disponible (En Préstamo)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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