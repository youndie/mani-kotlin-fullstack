package ru.workinprogress.mani

data class ServerConfig(
    val name: String,
    val scheme: String,
    val host: String,
    val development: Boolean = false,
    val port: String? = null
)

val staging = ServerConfig(
    "Staging",
    "https",
    "mani.kotlin.website"
)

val currentServerConfig: ServerConfig = staging
