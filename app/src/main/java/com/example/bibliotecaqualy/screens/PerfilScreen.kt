package com.example.bibliotecaqualy.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Notifications
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
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

@Composable
fun PerfilScreen(
    viewModel: BookViewModel,
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUserId = currentUser?.uid ?: ""
    val context = LocalContext.current

    var username by remember {
        mutableStateOf(currentUser?.displayName?.ifBlank { null } ?: currentUser?.email?.substringBefore("@") ?: "Usuario")
    }
    var photoUrl by remember { mutableStateOf(currentUser?.photoUrl?.toString() ?: "") }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditPhotoDialog by remember { mutableStateOf(false) }
    var newUsernameInput by remember { mutableStateOf("") }
    var newPhotoUrlInput by remember { mutableStateOf("") }

    // Datos reactivos
    val myBooksCount = viewModel.books.count { it.ownerId == currentUserId }
    val loansCount = viewModel.getActiveLoansCount(currentUserId)
    val recentActivities = viewModel.getRecentActivity(currentUserId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable {
                    newPhotoUrlInput = photoUrl
                    showEditPhotoDialog = true
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFD9D9D9)),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = username.take(1).uppercase(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = QualyGreen
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(QualyGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar Foto",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = username,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    newUsernameInput = username
                    showEditNameDialog = true
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar nombre",
                    tint = QualyGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = "Estudiante de Ingeniería Mecánica",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = "Universidad Nacional Autónoma de México",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjetas de Estadísticas (Publicados y Préstamos actualizados)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                number = myBooksCount.toString(),
                label = "Publicados",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            StatCard(
                number = loansCount.toString(),
                label = "Préstamos",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Sección: Actividad Reciente
        Text(
            text = "Actividad Reciente",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (recentActivities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes actividad registrada.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            recentActivities.forEach { item ->
                ActivityCard(
                    title = item.title,
                    description = item.description,
                    timeAgo = item.timeAgo
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Cerrar Sesión
        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cerrar Sesión",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }

    // DIÁLOGOS DE EDICIÓN
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Cambiar Username") },
            text = {
                OutlinedTextField(
                    value = newUsernameInput,
                    onValueChange = { newUsernameInput = it },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newUsernameInput.isNotBlank()) {
                            val profileUpdates = userProfileChangeRequest {
                                displayName = newUsernameInput.trim()
                            }
                            currentUser?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        username = newUsernameInput.trim()
                                        Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            showEditNameDialog = false
                        }
                    }
                ) {
                    Text("Guardar", color = QualyGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (showEditPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showEditPhotoDialog = false },
            title = { Text("Cambiar Foto de Perfil") },
            text = {
                OutlinedTextField(
                    value = newPhotoUrlInput,
                    onValueChange = { newPhotoUrlInput = it },
                    label = { Text("URL de la foto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uriToSave = android.net.Uri.parse(newPhotoUrlInput.trim())
                        val profileUpdates = userProfileChangeRequest { photoUri = uriToSave }
                        currentUser?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    photoUrl = newPhotoUrlInput.trim()
                                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                                }
                            }
                        showEditPhotoDialog = false
                    }
                ) {
                    Text("Guardar", color = QualyGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPhotoDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

// Componente para la Tarjeta de Actividad (Estilo Mockup)
@Composable
private fun ActivityCard(
    title: String,
    description: String,
    timeAgo: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F4F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = QualyGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = timeAgo,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun StatCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = QualyGreen
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}