package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate

data class GastoModel(
    val id: String,
    val nombre: String,
    val monto: Double,
    val fecha: LocalDate,
    val categoria: Categoria? = null,
    val categoriaPersonalizadaId: String? = null
)