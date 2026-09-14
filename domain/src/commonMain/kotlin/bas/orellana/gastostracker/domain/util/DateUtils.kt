package bas.orellana.gastostracker.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

fun todayLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
