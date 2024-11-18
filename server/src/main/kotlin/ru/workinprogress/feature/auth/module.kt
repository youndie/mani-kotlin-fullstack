package ru.workinprogress.feature.auth

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.workinprogress.feature.auth.data.AuthService
import ru.workinprogress.feature.user.data.TokenRepository

val authModule = module {
    singleOf(::AuthService)
    singleOf(::TokenRepository)
}