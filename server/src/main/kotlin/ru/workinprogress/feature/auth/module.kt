package ru.workinprogress.feature.auth

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.workinprogress.feature.auth.data.AuthService

val authModule = module {
    singleOf(::AuthService)
}