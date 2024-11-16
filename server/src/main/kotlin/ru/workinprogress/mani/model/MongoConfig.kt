package ru.workinprogress.mani.model

import io.ktor.server.config.ApplicationConfig
import org.bson.BsonValue

data class MongoConfig(val userName: String, val password: String, val host: String) {
    companion object {
        fun ApplicationConfig.mongoConfig(): MongoConfig {
            return MongoConfig(
                property("username").getString(),
                property("password").getString(),
                property("host").getString()
            )
        }
    }
}

val BsonValue.stringValue: String
    get() {
        return this.asObjectId().value.toHexString()
    }