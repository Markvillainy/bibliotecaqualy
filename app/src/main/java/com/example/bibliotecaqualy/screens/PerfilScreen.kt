package com.example.bibliotecaqualy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PerfilScreen(
    viewModel: BookViewModel,
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userEmail = currentUser?.email ?: "usuario@unam.mx"
    val userName = userEmail.substringBefore("@").replace(".", " ").capitalizeWords()

    // Métricas calculadas en tiempo real desde Firestore
    val myBooksCount = viewModel.books.count { it.ownerId == currentUser?.uid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Avatar circular del usuario
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier.size(50.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Datos del estudiante
        Text(
            text = userName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Estudiante de Ingeniería Mecánica",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = "Universidad Nacional Autónoma de México",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Métricas (Publicados, Intercambios, Préstamos)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricCard(number = "$myBooksCount", label = "Publicados", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MetricCard(number = "12", label = "Intercambios", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MetricCard(number = "2", label = "Préstamos", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección Actividad Reciente
        Text(
            text = "Actividad Reciente",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(12.dp))

        ActivityCard(
            title = "Intercambio completado",
            detail = "Cálculo Infinitesimal por Don Quijote",
            time = "Hace 2 días"
        )
        ActivityCard(
            title = "Préstamo registrado",
            detail = "Física Universitaria a Sofía Castro",
            time = "Hace 5 días"
        )
        ActivityCard(
            title = "Libro publicado",
            detail = "Álgebra Lineal - Stanley Grossman",
            time = "Hace 1 semana"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Cerrar Sesión
        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MetricCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = QualyGreen
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ActivityCard(title: String, detail: String, time: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(QualyBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = QualyGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = detail, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Text(text = time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

// Función auxiliar para formatear nombre
fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }