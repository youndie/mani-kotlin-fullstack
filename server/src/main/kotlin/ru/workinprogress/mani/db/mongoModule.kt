package ru.workinprogress.mani.db

import com.mongodb.kotlin.client.coroutine.MongoClient
import org.koin.dsl.module
import ru.workinprogress.mani.model.MongoConfig

fun mongoModule(mongoConfig: MongoConfig) = module {
    single<MongoClient> {
        MongoClient.create(
            "mongodb://${mongoConfig.host}/?&w=majority&appName=Mani"
        )
    }
    single { get<MongoClient>().getDatabase("mani") }
}