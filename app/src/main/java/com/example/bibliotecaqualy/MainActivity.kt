package com.example.bibliotecaqualy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.screens.AddEditBookScreen
import com.example.bibliotecaqualy.screens.MyBooksScreen
import com.example.bibliotecaqualy.viewmodel.BookViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var currentTab by remember { mutableStateOf(0) }
                var bookToEdit by remember { mutableStateOf<Book?>(null) }

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = {
                                    bookToEdit = null
                                    currentTab = 0
                                },
                                icon = { Icon(Icons.Default.List, contentDescription = "Mis Libros") },
                                label = { Text("Mis Libros") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.Add, contentDescription = "Publicar") },
                                label = { Text("Publicar") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> MyBooksScreen(
                                viewModel = viewModel,
                                onEditBook = { book: Book ->
                                    bookToEdit = book
                                    currentTab = 1
                                }
                            )
                            1 -> AddEditBookScreen(
                                viewModel = viewModel,
                                bookToEdit = bookToEdit,
                                onComplete = {
                                    bookToEdit = null
                                    currentTab = 0
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}