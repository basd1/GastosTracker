package bas.orellana.gastostracker.domain.model

/** Preferencias del usuario (no son datos: no entran en la copia de seguridad). */
data class Ajustes(
    val temaOscuro: Boolean = false,
    /** Código ISO 4217 (EUR, USD...). */
    val moneda: String = MONEDA_POR_DEFECTO,
    /** Día en que empieza el mes contable (1-28). Ver [Mes.periodo]. */
    val diaInicioMes: Int = 1,
    val recordatorioActivo: Boolean = false,
    val recordatorioHora: Int = 21,
    val recordatorioMinuto: Int = 0,
    val bloqueoActivo: Boolean = false,
    /** Último mes (`2026-10`) del que ya se enseñó el resumen mensual. */
    val ultimoResumenVisto: String? = null
) {
    companion object {
        const val MONEDA_POR_DEFECTO = "EUR"
        val DIAS_INICIO_VALIDOS = 1..28

        /** Monedas que se ofrecen en Ajustes. */
        val MONEDAS = listOf("EUR", "USD", "GBP", "CHF", "MXN", "ARS", "CLP", "COP", "PEN", "UYU", "BRL", "JPY", "CAD", "AUD")
    }
}
