package ru.workinprogress.mani.utilz

import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Routing.wasmJsApp() {
    singlePageApplication {
        useResources = true
        filesPath = "static"
        defaultPage = "index.html"
        ignoreFiles { it.endsWith(".txt") }
    }
}