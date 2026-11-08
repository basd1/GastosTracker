package bas.orellana.gastostracker.domain.model

import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Un mes concreto de un año ([numero] de 1 a 12). Sustituye a los pares (año, mes) sueltos. */
data class Mes(val anio: Int, val numero: Int) : Comparable<Mes> {

    init {
        require(numero in 1..12) { "Mes fuera de rango: $numero" }
    }

    fun anterior(): Mes = mas(-1)

    fun siguiente(): Mes = mas(1)

    /** Este mes desplazado [meses] hacia delante (o hacia atrás si es negativo). */
    fun mas(meses: Int): Mes {
        val indice = indice + meses
        return Mes(indice.floorDiv(12), indice.mod(12) + 1)
    }

    /** Meses desde enero del año 0: sirve para ordenar y restar meses. */
    val indice: Int get() = anio * 12 + (numero - 1)

    val primerDia: LocalDate get() = LocalDate(anio, numero, 1)

    val ultimoDia: LocalDate get() = primerDia.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

    /** Primer y último día del mes, ambos incluidos. */
    val rango: ClosedRange<LocalDate> get() = primerDia..ultimoDia

    val numeroDeDias: Int get() = ultimoDia.dayOfMonth

    operator fun contains(fecha: LocalDate): Boolean = fecha.year == anio && fecha.monthNumber == numero

    override fun compareTo(other: Mes): Int = indice.compareTo(other.indice)

    /** `2026-11`. */
    override fun toString(): String = "$anio-${numero.toString().padStart(2, '0')}"

    companion object {
        fun de(fecha: LocalDate): Mes = Mes(fecha.year, fecha.monthNumber)

        fun actual(): Mes = de(todayLocalDate())

        /** Lee el formato de [toString] (`2026-11`); null si no es válido. */
        fun parse(texto: String): Mes? {
            val partes = texto.split('-')
            if (partes.size != 2) return null
            val anio = partes[0].toIntOrNull() ?: return null
            val numero = partes[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
            return Mes(anio, numero)
        }
    }
}
