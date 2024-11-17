package ru.workinprogress.mani

import ru.workinprogress.feature.auth.authModule
import ru.workinprogress.feature.transaction.transactionsModule
import ru.workinprogress.mani.data.networkModule


val appModules = listOf(
    networkModule,
    authModule,
    transactionsModule
)

