package ru.workinprogress.mani

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.verify
import kotlin.test.Test


class ClientKoinModuleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        module {
            includes(appModules)
        }.verify(
            extraTypes = listOf(
                io.ktor.client.engine.HttpClientEngine::class,
                io.ktor.client.HttpClientConfig::class
            ),
        )
    }
}


