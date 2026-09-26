package com.example.bibliotecaqualy.util

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.swipeGestures(
    onSwipeRight: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    swipeThreshold: Float = 100f // Distancia mínima en píxeles para activar el cambio
): Modifier = this.pointerInput(Unit) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDrag = 0f },
        onDragEnd = {
            if (totalDrag > swipeThreshold) {
                onSwipeRight?.invoke() // Deslizar hacia la derecha (ej. Ir a la pantalla anterior/siguiente)
            } else if (totalDrag < -swipeThreshold) {
                onSwipeLeft?.invoke()  // Deslizar hacia la izquierda
            }
        },
        onHorizontalDrag = { _, dragAmount ->
            totalDrag += dragAmount
        }
    )
}