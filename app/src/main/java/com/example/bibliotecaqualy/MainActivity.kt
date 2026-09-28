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
import com.example.bibliotecaqualy.util.swipeGestures
import com.example.bibliotecaqualy.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Escucha en TIEMPO REAL los cambios de sesión (Suscripción a Firebase)
                var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

                DisposableEffect(Unit) {
                    val authListener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                    }
                    FirebaseAuth.getInstance().addAuthStateListener(authListener)
                    onDispose {
                        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
                    }
                }

                if (currentUser == null) {
                    // Pantalla de Inicio de Sesión
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            // Al ser exitoso, authListener cambiará automáticamente el estado
                        }
                    )
                } else {
                    // Pantalla Principal de la App (5 pestañas)
                    MainAppContent(
                        viewModel = viewModel,
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
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
    var showNotificationsScreen by remember { mutableStateOf(false) }

    // Permisos de notificaciones para Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showNotificationsScreen) {
        NotificationsScreen(
            viewModel = viewModel,
            onBack = { showNotificationsScreen = false }
        )
    } else if (selectedBookForDetail != null) {
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
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    // Habilita el cambio de pestañas deslizando la pantalla a los lados
                    .swipeGestures(
                        onSwipeLeft = {
                            if (currentTab < 4) {
                                if (currentTab != 1) bookToEdit = null
                                currentTab += 1
                            }
                        },
                        onSwipeRight = {
                            if (currentTab > 0) {
                                if (currentTab != 3) bookToEdit = null
                                currentTab -= 1
                            }
                        }
                    )
            ) {
                when (currentTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onBookClick = { book -> selectedBookForDetail = book },
                        onNotificationClick = { showNotificationsScreen = true },
                        onSwipeLeft = { if (currentTab < 4) currentTab += 1 },
                        onSwipeRight = { if (currentTab > 0) currentTab -= 1 }
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