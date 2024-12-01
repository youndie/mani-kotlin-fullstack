package ru.workinprogress.mani

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {

    LaunchedEffect("") {
        document.getElementById("progressBar")?.let { element ->
                document.body?.removeChild(element)
            }
        }
        App()
    }
}