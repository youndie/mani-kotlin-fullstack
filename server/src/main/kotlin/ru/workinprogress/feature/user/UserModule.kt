package ru.workinprogress.feature.user

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.workinprogress.feature.user.data.UserRepository
import ru.workinprogress.feature.auth.data.hashing.HashingService
import ru.workinprogress.feature.auth.data.hashing.SHA256HashingService

val userModule = module {
    singleOf(::UserRepository)
    singleOf(::SHA256HashingService).bind<HashingService>()
}