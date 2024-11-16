package ru.workinprogress.feature.currency

import io.github.smiley4.ktorswaggerui.dsl.routing.resources.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing

fun Routing.currencyRouting() {
    get<CurrencyResource>({
        description = "Get available currencies"
        request {}
        response {
            HttpStatusCode.OK to { body<List<Currency>>() }
        }
    }) {
        call.respond(HttpStatusCode.OK, listOf(Currency.Rub, Currency.Usd))
    }
}