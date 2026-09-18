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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bibliotecaqualy.model.Book
import com.example.bibliotecaqualy.ui.theme.QualyBackground
import com.example.bibliotecaqualy.ui.theme.QualyGreen
import com.example.bibliotecaqualy.viewmodel.BookViewModel

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
    var imageUri by remember { mutableStateOf<Uri?>(if (bookToEdit?.coverUrl?.isNotEmpty() == true) Uri.parse(bookToEdit.coverUrl) else null) }

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
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
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
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Subir Foto de Portada", color = Color.Gray)
                    Text("Formatos permitidos: JPG, PNG", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del Libro") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Autor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = edition,
            onValueChange = { edition = it },
            label = { Text("Edición / Año") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.isNotEmpty() && author.isNotEmpty()) {
                    val bookToSave = Book(
                        id = bookToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title,
                        author = author,
                        category = category,
                        edition = edition,
                        state = state,
                        coverUrl = imageUri?.toString() ?: "",
                        rating = 5.0
                    )

                    if (bookToEdit == null) {
                        viewModel.addBook(bookToSave)
                    } else {
                        viewModel.updateBook(bookToSave)
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
            Text(if (bookToEdit == null) "Publicar Libro" else "Guardar Cambios", color = Color.White)
        }
    }
}