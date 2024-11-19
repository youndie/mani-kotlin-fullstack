package ru.workinprogress.mani

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        state = rememberWindowState(
            size = DpSize(420.dp, (420 / 9.toFloat() * 16).dp)
        ),
        onCloseRequest = ::exitApplication,
        resizable = false,
        title = "mani",
    ) {
        App()
    }
}