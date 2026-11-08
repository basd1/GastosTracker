package bas.orellana.gastostracker.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

fun todayLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

/** Primer y último día (ambos incluidos) del mes indicado; `month` va de 1 a 12. */
fun rangoDelMes(year: Int, month: Int): ClosedRange<LocalDate> {
    val primerDia = LocalDate(year, month, 1)
    val ultimoDia = primerDia.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    return primerDia..ultimoDia
}
