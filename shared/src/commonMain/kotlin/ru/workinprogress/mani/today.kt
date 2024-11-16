package ru.workinprogress.mani

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime


fun today() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
val defaultMinDate get() = today().minus(1, DateTimeUnit.MONTH)