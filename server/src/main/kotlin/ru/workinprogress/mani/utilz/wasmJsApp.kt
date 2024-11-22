package ru.workinprogress.mani.utilz

import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Routing.wasmJsApp() {
    staticResources("/", "static", index = "index.html") {
        default("index.html")
        cacheControl { file ->
            if (file.file.contains("composeApp") || file.file.contains("index")) {
                listOf(Immutable, CacheControl.MaxAge(10000))
            } else {
                emptyList<CacheControl>()
            }
        }
    }
}

object Immutable : CacheControl(null) {
    override fun toString(): String = "immutable"
}