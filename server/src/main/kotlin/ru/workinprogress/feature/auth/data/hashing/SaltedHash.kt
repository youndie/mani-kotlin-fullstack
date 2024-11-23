package ru.workinprogress.feature.auth.data.hashing

data class SaltedHash(
  val hash: String,
  val salt: String
)
