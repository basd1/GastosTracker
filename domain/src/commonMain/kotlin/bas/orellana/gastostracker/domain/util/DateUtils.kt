package bas.orellana.gastostracker.domain.util

import bas.orellana.gastostracker.domain.model.Mes
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

fun todayLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

/** Primer y último día (ambos incluidos) del mes indicado; `month` va de 1 a 12. */
fun rangoDelMes(year: Int, month: Int): ClosedRange<LocalDate> = Mes(year, month).rango
