package ru.workinprogress.feature.currency

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.workinprogress.feature.currency.data.CurrentCurrencyRepository

val currencyModule = module {
    singleOf(::GetCurrentCurrencyUseCase)
    singleOf(::CurrentCurrencyRepository)
}