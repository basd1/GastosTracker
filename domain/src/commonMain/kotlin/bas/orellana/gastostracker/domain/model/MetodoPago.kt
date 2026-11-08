package bas.orellana.gastostracker.domain.model

/** Cómo se pagó un gasto. Opcional: los gastos antiguos no lo tienen. */
enum class MetodoPago {
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA,
    BIZUM,
    OTRO;

    companion object {
        fun desdeNombre(nombre: String?): MetodoPago? = nombre?.let { n -> entries.find { it.name == n } }
    }
}
