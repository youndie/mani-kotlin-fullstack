package ru.workinprogress.mani

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.verify
import ru.workinprogress.mani.model.JWTConfig
import ru.workinprogress.mani.model.MongoConfig
import kotlin.test.Test


class ServerKoinModuleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        module {
            includes(appModules(MongoConfig(), JWTConfig()))
        }.verify(
            injections = listOf(
                definition<com.mongodb.kotlin.client.coroutine.MongoClient>(MongoClient::class),
                definition<com.mongodb.kotlin.client.coroutine.MongoDatabase>(MongoDatabase::class)
            )
        )
    }
}