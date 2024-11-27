package ru.workinprogress.mani.utilz

import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.server.routing.*

fun Routing.swagger() {
    route("swagger") {
        swaggerUI("/api.json")
    }
    route("api.json") {
        openApiSpec()
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
