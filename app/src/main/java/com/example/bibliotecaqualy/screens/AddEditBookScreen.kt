package com.example.bibliotecaqualy.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.ui.theme.*
import com.example.bibliotecaqualy.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    viewModel: BookViewModel,
    bookToEdit: Book? = null,
    onComplete: () -> Unit
) {
    var title by remember { mutableStateOf(bookToEdit?.title ?: "") }
    var author by remember { mutableStateOf(bookToEdit?.author ?: "") }
    var category by remember { mutableStateOf(bookToEdit?.category ?: "Literatura") }
    var edition by remember { mutableStateOf(bookToEdit?.edition ?: "") }
    var state by remember { mutableStateOf(bookToEdit?.state ?: "Excelente") }

    // CORRECCIÓN LÍNEA 40: Asignar directamente coverUri si ya es de tipo Uri?
    var imageUri by remember { mutableStateOf<Uri?>(bookToEdit?.coverUri) }

    // Opciones para la categoría
    val categories = listOf("Literatura", "Matemáticas", "Ciencias", "Didáctico", "Otros")
    var expandedCategory by remember { mutableStateOf(false) }

    // Launcher para abrir la Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QualyBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (bookToEdit == null) "Publicar un Libro" else "Editar Libro",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenedor para Subir / Mostrar Portada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Portada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Subir Foto de Portada", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Text("Formatos permitidos: JPG, PNG", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Título
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del Libro") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Autor
        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Autor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Selector Desplegable de Categoría (Dropdown)
        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Materia / Categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            category = item
                            expandedCategory = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Edición
        OutlinedTextField(
            value = edition,
            onValueChange = { edition = it },
            label = { Text("Edición / Año") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Guardar / Publicar
        Button(
            onClick = {
                if (title.isNotEmpty() && author.isNotEmpty()) {
                    // CORRECCIÓN LÍNEA 173: Pasar 'coverUri = imageUri' directamente
                    val newOrUpdatedBook = Book(
                        id = bookToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title,
                        author = author,
                        category = category,
                        edition = edition,
                        state = state,

                    )

                    if (bookToEdit == null) {
                        viewModel.addBook(newOrUpdatedBook)
                    } else {
                        viewModel.updateBook(newOrUpdatedBook)
                    }
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QualyGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                if (bookToEdit == null) "Publicar Libro" else "Guardar Cambios",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}