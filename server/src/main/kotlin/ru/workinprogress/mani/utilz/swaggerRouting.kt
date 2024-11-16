package ru.workinprogress.mani.utilz

import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route

fun Routing.swagger() {
    route("swagger") {
        swaggerUI("/api.json")
    }
    route("api.json") {
        openApiSpec()
    }
}