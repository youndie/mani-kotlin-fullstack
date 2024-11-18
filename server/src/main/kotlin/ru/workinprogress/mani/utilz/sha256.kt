package ru.workinprogress.mani.utilz

import java.security.MessageDigest

fun String.sha256(): String {
    return hashString(this, "SHA-256")
}

@OptIn(ExperimentalStdlibApi::class)
private fun hashString(input: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .toHexString()
}