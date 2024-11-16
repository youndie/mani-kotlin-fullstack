package ru.workinprogress.feature.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokensResponse(val accessToken: String, val refreshToken: String)

