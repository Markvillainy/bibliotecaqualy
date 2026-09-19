package com.example.bibliotecaqualy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.bibliotecaqualy.screens.HomeScreen
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.screens.AddEditBookScreen
import com.example.bibliotecaqualy.screens.BuzonScreen
import com.example.bibliotecaqualy.screens.LoginScreen
import com.example.bibliotecaqualy.screens.MyBooksScreen
import com.example.bibliotecaqualy.screens.PerfilScreen
import com.example.bibliotecaqualy.ui.theme.BibliotecaQualyTheme
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth
 import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BibliotecaQualyTheme {
                val auth = FirebaseAuth.getInstance()
                var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                } else {
                    MainAppStructure(viewModel = viewModel, onLogout = { isLoggedIn = false })
                }
            }
        }
    }
}

@Composable
fun MainAppStructure(viewModel: BookViewModel, onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(0) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple("Inicio", Icons.Default.Home, 0),
                    Triple("Mis Libros", Icons.Default.List, 1),
                    Triple("Publicar", Icons.Default.Add, 2),
                    Triple("Buzón", Icons.Default.Mail, 3),
                    Triple("Perfil", Icons.Default.Person, 4)
                )

                navItems.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = {
                            if (index != 2) bookToEdit = null
                            currentTab = index
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = QualyGreen,
                            indicatorColor = QualyGreen,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                0 -> HomeScreen(viewModel = viewModel)
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
                3 -> BuzonScreen() // (Próxima pantalla)
                4 -> {
                    val context = LocalContext.current
                    PerfilScreen(
                        viewModel = viewModel,
                        onLogout = {
                            val activity = context as? ComponentActivity
                            activity?.finish()
                            activity?.startActivity(activity.intent)
                        }
                    )
                }
            }
        }
    }
}