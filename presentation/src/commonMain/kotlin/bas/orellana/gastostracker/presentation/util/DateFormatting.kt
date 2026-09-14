package bas.orellana.gastostracker.presentation.util

import kotlinx.datetime.LocalDate

fun LocalDate.formatDDMMYYYY(): String {
    val day = dayOfMonth.toString().padStart(2, '0')
    val month = monthNumber.toString().padStart(2, '0')
    return "$day/$month/$year"
}
