package bas.orellana.gastostracker.domain.model

import java.time.LocalDate

data class GastoModel(
    val id: String,
    val nombre: String,
    val monto: Double,
    val fecha: LocalDate
)