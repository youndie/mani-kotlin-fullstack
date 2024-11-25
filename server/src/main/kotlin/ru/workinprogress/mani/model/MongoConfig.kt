package ru.workinprogress.mani.model

import io.ktor.server.config.*
import org.bson.BsonValue
import java.io.File

data class MongoConfig(val userName: String = "", val password: String = "", val host: String = "") {
    companion object {
        fun ApplicationConfig.mongoConfig(): MongoConfig {
            return MongoConfig(
                property("username").getString(),
                property("password").getString().let { password ->
                    File("/run/secrets/${password}").takeIf { it.exists() }?.readText() ?: password
                },
                property("host").getString()
            )
        }
    }
}

val BsonValue.stringValue: String
    get() {
        return this.asObjectId().value.toHexString()
    }