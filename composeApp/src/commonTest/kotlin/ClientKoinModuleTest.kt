package ru.workinprogress.mani

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import kotlin.test.Test


class ClientKoinModuleTest {

    //https://github.com/InsertKoinIO/koin/issues/2029
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        module {
            includes(appModules)
        }.verify(
            injections = listOf(
                definition<io.ktor.client.HttpClient>(io.ktor.client.engine.HttpClientEngine::class),
                definition<io.ktor.client.HttpClient>(io.ktor.client.HttpClientConfig::class)
            )
        )
    }
}