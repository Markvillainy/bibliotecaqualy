package com.example.bibliotecaqualy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.ui.theme.*
import com.example.bibliotecaqualy.viewmodel.BookViewModel

@Composable
fun MyBooksScreen(
    viewModel: BookViewModel,
    onEditBook: (Book) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Libros Publicados",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(viewModel.books) { book ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = QualyCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Portada
                        Box(
                            modifier = Modifier
                                .size(70.dp, 90.dp)
                                .background(QualyChipUnselected, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (book.coverUri != null) {
                                AsyncImage(
                                    model = book.coverUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(book.category, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Detalles + Botones de Acción (CRUD)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = book.author, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onEditBook(book) },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Editar", color = Color.DarkGray)
                                }

                                Button(
                                    onClick = { viewModel.deleteBook(book.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Retirar", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}