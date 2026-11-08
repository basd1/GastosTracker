package bas.orellana.gastostracker.data.local.entity

import androidx.room.ColumnInfo

/** Fila de resultado de las consultas `SUM(monto) ... GROUP BY categoría`. */
data class TotalPorCategoriaRow(
    val categoria: String?,
    @ColumnInfo(name = "categoria_personalizada_id") val categoriaPersonalizadaId: String?,
    val total: Double
)
