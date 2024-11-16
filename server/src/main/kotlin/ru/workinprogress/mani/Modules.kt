package ru.workinprogress.mani

import com.mongodb.kotlin.client.coroutine.MongoClient
import org.koin.dsl.module
import ru.workinprogress.feature.auth.authModule
import ru.workinprogress.feature.transaction.transactionModule
import ru.workinprogress.feature.user.userModule
import ru.workinprogress.mani.db.mongoModule
import ru.workinprogress.mani.model.JWTConfig
import ru.workinprogress.mani.model.MongoConfig

fun modules(mongoConfig: MongoConfig, jwtConfig: JWTConfig) =
    listOf(
        module {
            single<JWTConfig> { jwtConfig }
        },
        mongoModule(mongoConfig)
    ) + featureModules()

private fun featureModules() = listOf(
    authModule,
    userModule,
    transactionModule
)

