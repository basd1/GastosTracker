package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate

data class IngresoModel(
    val id: String,
    val nombre: String,
    val monto: Double,
    val fecha: LocalDate,
    val categoria: CategoriaIngreso? = null,
    val categoriaPersonalizadaId: String? = null,
    val nota: String? = null
)
