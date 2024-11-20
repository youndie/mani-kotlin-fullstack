package ru.workinprogress.mani

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ru.workinprogress.feature.auth.authRouting
import ru.workinprogress.feature.currency.currencyRouting
import ru.workinprogress.feature.transaction.transactionRouting
import ru.workinprogress.feature.user.userRouting
import ru.workinprogress.mani.utilz.swagger
import ru.workinprogress.mani.utilz.wasmJsApp


fun Application.configureRouting() {
    routing {
        swagger()
        authRouting()
        currencyRouting()
        transactionRouting()
        userRouting()
        wasmJsApp()
    }
}
