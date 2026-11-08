package bas.orellana.gastostracker.data.local

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

/**
 * Las fechas se guardan como texto ISO-8601 (`2026-11-08`): se ordenan y comparan
 * correctamente como texto, así que `BETWEEN` y `ORDER BY` funcionan en SQL.
 */
class Converters {
    @TypeConverter
    fun localDateToString(fecha: LocalDate?): String? = fecha?.toString()

    @TypeConverter
    fun stringToLocalDate(texto: String?): LocalDate? = texto?.let { LocalDate.parse(it) }
}
