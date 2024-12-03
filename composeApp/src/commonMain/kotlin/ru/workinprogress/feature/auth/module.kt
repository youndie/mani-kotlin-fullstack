package ru.workinprogress.feature.auth

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.workinprogress.feature.auth.data.TokenRepository
import ru.workinprogress.feature.auth.data.TokenRepositoryCommon
import ru.workinprogress.feature.auth.data.TokenStorage
import ru.workinprogress.feature.auth.data.TokenStorageCommon
import ru.workinprogress.feature.auth.domain.LogoutUseCase

expect val authModulePlatform: Module

val authModule = module {
    single<TokenStorage> { TokenStorageCommon() }
    single<TokenRepository> { TokenRepositoryCommon(get()) }
    includes(authModulePlatform)

    singleOf(::LogoutUseCase)
}

