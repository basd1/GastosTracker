package bas.orellana.gastostracker.domain.model

/** Suma de importes de una categoría (predefinida o personalizada) en un rango de fechas. */
data class TotalPorCategoria(
    val categoria: Categoria?,
    val categoriaPersonalizadaId: String?,
    val total: Double
)
