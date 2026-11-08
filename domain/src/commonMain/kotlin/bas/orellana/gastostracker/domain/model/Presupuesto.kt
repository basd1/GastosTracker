package bas.orellana.gastostracker.domain.model

/**
 * Límite de gasto mensual. Si no tiene categoría (ni predefinida ni personalizada) es el
 * presupuesto global, que se compara con todo lo gastado en el mes.
 */
data class Presupuesto(
    val categoria: Categoria?,
    val categoriaPersonalizadaId: String?,
    val monto: Double
) {
    init {
        require(categoria == null || categoriaPersonalizadaId == null) { "Un presupuesto solo puede tener una categoría" }
    }

    val esGlobal: Boolean get() = categoria == null && categoriaPersonalizadaId == null

    /** Identifica el presupuesto: solo puede haber uno por categoría y uno global. */
    val clave: String get() = claveDe(categoria, categoriaPersonalizadaId)

    companion object {
        const val CLAVE_GLOBAL = "global"

        fun claveDe(categoria: Categoria?, categoriaPersonalizadaId: String?): String = when {
            categoria != null -> "cat:${categoria.name}"
            categoriaPersonalizadaId != null -> "per:$categoriaPersonalizadaId"
            else -> CLAVE_GLOBAL
        }

        fun global(monto: Double) = Presupuesto(null, null, monto)
    }
}

/** Verde hasta el 80 %, ámbar desde el 80 % hasta el límite, rojo al pasarse. */
enum class NivelPresupuesto { BIEN, AVISO, SUPERADO }

/** Cómo va un presupuesto en un mes concreto. */
data class EstadoPresupuesto(
    val presupuesto: Presupuesto,
    val gastado: Double
) {
    /** Fracción gastada (1.0 = el límite exacto). */
    val progreso: Double get() = if (presupuesto.monto > 0) gastado / presupuesto.monto else 0.0

    val restante: Double get() = presupuesto.monto - gastado

    val nivel: NivelPresupuesto
        get() = when {
            progreso > 1.0 -> NivelPresupuesto.SUPERADO
            progreso >= UMBRAL_AVISO -> NivelPresupuesto.AVISO
            else -> NivelPresupuesto.BIEN
        }

    companion object {
        const val UMBRAL_AVISO = 0.8
    }
}

/**
 * Cruza los presupuestos con lo gastado por categoría en un periodo.
 * El global va primero; el resto, del más apurado al más holgado.
 */
fun calcularEstadoPresupuestos(
    presupuestos: List<Presupuesto>,
    totalesPorCategoria: List<TotalPorCategoria>
): List<EstadoPresupuesto> {
    val gastos = totalesPorCategoria
    val totalGeneral = gastos.sumOf { it.total }
    return presupuestos
        .map { presupuesto ->
            val gastado = if (presupuesto.esGlobal) {
                totalGeneral
            } else {
                gastos.filter {
                    it.categoria == presupuesto.categoria &&
                        it.categoriaPersonalizadaId == presupuesto.categoriaPersonalizadaId
                }.sumOf { it.total }
            }
            EstadoPresupuesto(presupuesto, gastado)
        }
        .sortedWith(compareByDescending<EstadoPresupuesto> { it.presupuesto.esGlobal }.thenByDescending { it.progreso })
}
