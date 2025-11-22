package ru.workinprogress.mani

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import ru.workinprogress.feature.auth.authRouting
import ru.workinprogress.feature.category.categoryRouting
import ru.workinprogress.feature.currency.currencyRouting
import ru.workinprogress.feature.transaction.transactionRouting
import ru.workinprogress.feature.user.userRouting
import ru.workinprogress.mani.utilz.wasmJsApp

fun Application.configureRouting() {
    routing {
        authRouting()
        categoryRouting()
        currencyRouting()
        transactionRouting()
        userRouting()
        wasmJsApp()
    }
}
