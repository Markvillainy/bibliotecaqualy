package com.example.bibliotecaqualy.util

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.swipeGestures(
    onSwipeRight: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    swipeThreshold: Float = 40f // Umbral reducido para mayor sensibilidad
): Modifier = this.pointerInput(Unit) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDrag = 0f },
        onDragEnd = {
            if (totalDrag > swipeThreshold) {
                onSwipeRight?.invoke()
            } else if (totalDrag < -swipeThreshold) {
                onSwipeLeft?.invoke()
            }
        },
        onHorizontalDrag = { change, dragAmount ->
            change.consume() // Consume el evento de arrastre para evitar interferencias
            totalDrag += dragAmount
        }
    )
}