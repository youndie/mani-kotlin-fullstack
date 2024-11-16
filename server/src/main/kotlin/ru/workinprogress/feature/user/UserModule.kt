package ru.workinprogress.feature.user

import org.koin.dsl.module
import ru.workinprogress.feature.user.data.UserRepository

val userModule = module {
    single<UserRepository> { UserRepository(get()) }
}