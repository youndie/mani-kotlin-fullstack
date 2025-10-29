package ru.workinprogress.mani.utilz

import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.routing.*

fun Routing.swagger() {
    route("api.json") {
        openApi()
    }
    route("swagger") {
        swaggerUI("/api.json") {
        }
    }
}

fun swaggerUrl(scheme: String, base: String, port: String?) = buildString {
    append(scheme)
    append("://")
    append(base)
    port?.let {
        append(":")
        append(it)
    }
    append("/")
}
