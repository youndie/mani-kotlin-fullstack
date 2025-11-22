package ru.workinprogress.mani

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import ru.workinprogress.appframe.AppFrame
import ru.workinprogress.mani.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    AppTheme {
        AppFrame(
            ::exitApplication,
            title = "Mani",
        ) {
            App(modifier = Modifier.fillMaxSize())
        }
    }
}
