package ru.workinprogress.mani

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

inline fun <T> emptyImmutableList(): ImmutableList<T> = emptyList<T>().toImmutableList()

fun today() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
val defaultMinDate get() = today().minus(1, DateTimeUnit.MONTH)