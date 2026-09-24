package com.example.bibliotecaqualy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliotecaqualy.model.ActivityLog
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PerfilScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onSignOut: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    // Extraer iniciales para el avatar
    val initials = profileViewModel.userName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "U" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar con Iniciales del Usuario Real
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFFD9D9D9)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = QualyGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nombre y Correo Universitario Reales
        Text(
            text = profileViewModel.userName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = profileViewModel.userCareer,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = profileViewModel.userEmail,
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjetas con Contadores Dinámicos Reales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(number = profileViewModel.publishedCount.toString(), label = "Publicados", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(number = profileViewModel.exchangesCount.toString(), label = "Intercambios", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(number = profileViewModel.loansCount.toString(), label = "Préstamos", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Historial de Actividad Reciente
        Text(
            text = "Actividad Reciente",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (profileViewModel.activities.isEmpty()) {
            Text(
                text = "Aún no tienes actividad registrada.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            profileViewModel.activities.forEach { activity ->
                ActivityItem(activity = activity)
                Spacer(modifier = Modifier.height(8.dp))
            }
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
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ActivityItem(activity: ActivityLog) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedTime = activity.timestamp.toDate()?.let { dateFormat.format(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
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
                Text(text = activity.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = activity.detail, color = Color.Gray, fontSize = 12.sp)
            }

            Text(text = formattedTime, color = Color.Gray, fontSize = 10.sp)
        }
    }
}