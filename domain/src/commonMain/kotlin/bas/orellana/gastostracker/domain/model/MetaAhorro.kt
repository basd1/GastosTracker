package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate
import kotlin.math.max

/**
 * Hucha con un propósito ("Viaje a Japón"). El objetivo y la fecha son opcionales: sin objetivo es
 * una hucha sin más; con objetivo y fecha, la app calcula cuánto hay que apartar cada mes.
 *
 * El ahorro ya no es una categoría de gasto: lo que se aparta se apunta como [Aportacion] de una meta
 * y no cuenta como gasto en ningún sitio.
 */
data class MetaAhorro(
    val id: String,
    val nombre: String,
    val objetivo: Double? = null,
    val fechaObjetivo: LocalDate? = null,
    val color: Long,
    val creada: LocalDate,
    val archivada: Boolean = false
) {
    init {
        require(objetivo == null || objetivo > 0) { "El objetivo tiene que ser mayor que 0" }
    }

    companion object {
        /** Meta a la que fue a parar lo que se apuntaba en la antigua categoría "Ahorro". */
        const val ID_AHORRO_GENERAL = "ahorro-general"
    }
}

/** Dinero que entra en una meta (positivo) o que se saca de ella (negativo). */
data class Aportacion(
    val id: String,
    val metaId: String,
    val monto: Double,
    val fecha: LocalDate,
    val nota: String? = null
) {
    val esRetirada: Boolean get() = monto < 0
}

/** Una meta con lo que lleva ahorrado (suma de sus aportaciones). */
data class ProgresoMeta(
    val meta: MetaAhorro,
    val ahorrado: Double
) {
    /** Fracción del objetivo conseguida; null si la meta no tiene objetivo. */
    val progreso: Double? get() = meta.objetivo?.let { ahorrado / it }

    val restante: Double? get() = meta.objetivo?.let { max(0.0, it - ahorrado) }

    val completada: Boolean get() = meta.objetivo != null && ahorrado >= meta.objetivo

    /** La fecha objetivo ya pasó sin llegar al objetivo. */
    fun vencida(hoy: LocalDate): Boolean = meta.fechaObjetivo != null && meta.fechaObjetivo < hoy && !completada

    /**
     * Cuánto hay que apartar cada mes, contando el actual, para llegar al objetivo en la fecha.
     * null si no hay objetivo o fecha, o si la fecha ya pasó; 0 si ya está conseguida.
     */
    fun cuotaMensual(hoy: LocalDate): Double? {
        val restante = restante ?: return null
        val fecha = meta.fechaObjetivo ?: return null
        if (completada) return 0.0
        val meses = Mes.de(fecha).indice - Mes.de(hoy).indice + 1
        if (meses <= 0 || fecha < hoy) return null
        return restante / meses
    }
}
