package ru.workinprogress.feature.transaction

import io.ktor.resources.*

@Resource("/transaction")
class TransactionResource {

    @Resource("/{id}")
    class ById(val parent: TransactionResource = TransactionResource(), val id: String)
}