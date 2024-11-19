package ru.workinprogress.mani

import org.koin.dsl.module
import ru.workinprogress.feature.auth.authModule
import ru.workinprogress.feature.transaction.transactionModule
import ru.workinprogress.feature.user.userModule
import ru.workinprogress.mani.db.mongoModule
import ru.workinprogress.mani.model.JWTConfig
import ru.workinprogress.mani.model.MongoConfig

fun appModules(mongoConfig: MongoConfig, jwtConfig: JWTConfig) =
    listOf(
        configModule(jwtConfig),
        mongoModule(mongoConfig)
    ) + featureModules()

private fun configModule(jwtConfig: JWTConfig) = module {
}

private fun featureModules() = listOf(
    authModule,
    userModule,
    transactionModule
)

