package com.example.bibliotecaqualy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.screens.*
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.viewmodel.BookViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Controla si el usuario está dentro o fuera de la app
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    // Muestra la pantalla de Login si no ha iniciado sesión
                    LoginScreen(
                        onLoginSuccess = {
                            isLoggedIn = true
                        }
                    )
                } else {
                    // Muestra las 5 pestañas de la app si ya inició sesión
                    MainAppContent(
                        viewModel = viewModel,
                        onLogout = {
                            isLoggedIn = false // Redirige inmediatamente al LoginScreen
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: BookViewModel,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableIntStateOf(0) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var selectedBookForDetail by remember { mutableStateOf<Book?>(null) }

    // Solicitar permiso de notificaciones en Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (selectedBookForDetail != null) {
        BookDetailScreen(
            book = selectedBookForDetail!!,
            viewModel = viewModel,
            onBack = { selectedBookForDetail = null }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    val items = listOf(
                        NavigationItem("Inicio", Icons.Default.Home, 0),
                        NavigationItem("Mis Libros", Icons.Default.List, 1),
                        NavigationItem("Publicar", Icons.Default.AddCircle, 2),
                        NavigationItem("Buzón", Icons.Default.Email, 3),
                        NavigationItem("Perfil", Icons.Default.Person, 4)
                    )

                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentTab == item.index,
                            onClick = {
                                if (item.index != 2) bookToEdit = null
                                currentTab = item.index
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = QualyGreen,
                                selectedTextColor = QualyGreen,
                                indicatorColor = Color(0xFFE8F5E9)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onBookClick = { book -> selectedBookForDetail = book }
                    )
                    1 -> MyBooksScreen(
                        viewModel = viewModel,
                        onEditBook = { book ->
                            bookToEdit = book
                            currentTab = 2
                        }
                    )
                    2 -> AddEditBookScreen(
                        viewModel = viewModel,
                        bookToEdit = bookToEdit,
                        onComplete = {
                            bookToEdit = null
                            currentTab = 1
                        }
                    )
                    3 -> BuzonScreen(viewModel = viewModel)
                    4 -> PerfilScreen(
                        viewModel = viewModel,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val index: Int
)