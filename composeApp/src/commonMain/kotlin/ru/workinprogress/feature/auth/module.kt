package ru.workinprogress.feature.auth

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.workinprogress.feature.auth.data.TokenRepository
import ru.workinprogress.feature.auth.data.TokenRepositoryCommon
import ru.workinprogress.feature.auth.data.TokenStorage
import ru.workinprogress.feature.auth.data.TokenStorageCommon

expect val authModulePlatform: Module

val authModule = module {
    single<TokenStorage> { TokenStorageCommon() }
    single<TokenRepository> { TokenRepositoryCommon(get()) }
    includes(authModulePlatform)
}

