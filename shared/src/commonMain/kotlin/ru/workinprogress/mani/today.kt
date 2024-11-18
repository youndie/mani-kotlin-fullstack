package ru.workinprogress.mani

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.*

inline fun <T> emptyImmutableList(): ImmutableList<T> = emptyList<T>().toImmutableList()

fun today() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
val LocalDate?.orToday get() = this ?: today()
val defaultMinDate get() = today().minus(1, DateTimeUnit.MONTH)