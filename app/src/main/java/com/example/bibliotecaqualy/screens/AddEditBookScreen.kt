package com.example.bibliotecaqualy.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    viewModel: BookViewModel,
    bookToEdit: Book? = null,
    onComplete: () -> Unit
) {
    var title by remember { mutableStateOf(bookToEdit?.title ?: "") }
    var author by remember { mutableStateOf(bookToEdit?.author ?: "") }
    var description by remember { mutableStateOf(bookToEdit?.description ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(bookToEdit?.coverUri) }

    // Estado para el NUEVO campo: Género
    val genreOptions = listOf("Literatura", "Matemáticas", "Ciencias", "Didáctico", "Otros")
    var expandedGenre by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf(bookToEdit?.genre ?: genreOptions[0]) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (bookToEdit == null) "Publicar Libro" else "Editar Libro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = QualyGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Portada
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { imagePickerLauncher.launch("image/*") },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Portada",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Toca para seleccionar imagen de portada", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del libro") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Autor
        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Autor") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- NUEVO CAMPO: Selector de Género ---
        ExposedDropdownMenuBox(
            expanded = expandedGenre,
            onExpandedChange = { expandedGenre = !expandedGenre },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedGenre,
                onValueChange = {},
                readOnly = true,
                label = { Text("Género") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenre) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedGenre,
                onDismissRequest = { expandedGenre = false }
            ) {
                genreOptions.forEach { genre ->
                    DropdownMenuItem(
                        text = { Text(genre) },
                        onClick = {
                            selectedGenre = genre
                            expandedGenre = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Descripción
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción / Estado del libro") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Guardar / Publicar
        Button(
            onClick = {
                // 1. Declaramos las variables del usuario actual de Firebase
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val currentUserId = currentUser?.uid ?: ""
                val currentUserName = currentUser?.displayName ?: currentUser?.email ?: "Usuario Qualy"

                // 2. Creamos el objeto Book usando esas variables
                // Dentro de AddEditBookScreen.kt en el evento onClick:
                val newBook = Book(
                    id = bookToEdit?.id ?: "",
                    title = title,
                    author = author,
                    category = selectedGenre, // <-- Asigna el género/materia seleccionado aquí
                    genre = selectedGenre,
                    description = description,
                    coverUrl = "",
                    ownerId = currentUserId,
                    ownerName = currentUserName,
                    availabilityStatus = bookToEdit?.availabilityStatus ?: "Disponible"
                ).apply {
                    coverUri = selectedImageUri
                }

                // 3. Guardamos o actualizamos según corresponda
                if (bookToEdit == null) {
                    viewModel.addBook(newBook)
                } else {
                    viewModel.updateBook(newBook)
                }

                onComplete()

                },

            colors = ButtonDefaults.buttonColors(containerColor = QualyGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (bookToEdit == null) "Publicar Libro" else "Guardar Cambios",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}