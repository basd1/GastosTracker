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


    /**
     * Periodo contable de este mes cuando el mes empieza el día [diaInicio] (1-28).
     * El periodo se llama como el mes en el que caen la mayoría de sus días:
     * - día 1: el mes natural;
     * - días 2-15: del día [diaInicio] de este mes al anterior del mes siguiente (día 5: 5 nov – 4 dic = «noviembre»);
     * - días 16-28: del día [diaInicio] del mes anterior al anterior de este mes (día 25: 25 oct – 24 nov = «noviembre»).
     */
    fun periodo(diaInicio: Int): ClosedRange<LocalDate> {
        if (diaInicio <= 1) return rango
        val mesInicio = if (diaInicio <= 15) this else anterior()
        val inicio = LocalDate(mesInicio.anio, mesInicio.numero, diaInicio)
        val siguiente = mesInicio.siguiente()
        val fin = LocalDate(siguiente.anio, siguiente.numero, diaInicio).minus(1, DateTimeUnit.DAY)
        return inicio..fin
    }

    operator fun contains(fecha: LocalDate): Boolean = fecha.year == anio && fecha.monthNumber == numero

    override fun compareTo(other: Mes): Int = indice.compareTo(other.indice)

    /** `2026-11`. */
    override fun toString(): String = "$anio-${numero.toString().padStart(2, '0')}"

    companion object {
        fun de(fecha: LocalDate): Mes = Mes(fecha.year, fecha.monthNumber)

        fun actual(): Mes = de(todayLocalDate())

        /** Periodo (ver [periodo]) al que pertenece [fecha] si el mes empieza el día [diaInicio]. */
        fun dePeriodo(fecha: LocalDate, diaInicio: Int): Mes {
            if (diaInicio <= 1) return de(fecha)
            val mesInicio = if (fecha.dayOfMonth >= diaInicio) de(fecha) else de(fecha).anterior()
            return if (diaInicio <= 15) mesInicio else mesInicio.siguiente()
        }

        fun actual(diaInicio: Int): Mes = dePeriodo(todayLocalDate(), diaInicio)

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
