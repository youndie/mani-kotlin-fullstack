package ru.workinprogress.mani

import org.koin.core.module.Module
import ru.workinprogress.feature.auth.authModule
import ru.workinprogress.feature.currency.currencyModule
import ru.workinprogress.feature.transaction.transactionsModule
import ru.workinprogress.mani.data.networkModule


val appModules: List<Module> = listOf(
    networkModule,
    authModule,
    currencyModule,
    transactionsModule
)

