package ru.workinprogress.mani.data

open class ServerException(
    override val message: String = "Server error",
    override val cause: Exception? = null
) : Exception(message, cause)