package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.datetime.LocalDate

data class AddIngresoState(
    val concepto: String = "",
    val monto: String = "",
    val categoriaSeleccionada: Categoria? = null,
    val categoriaPersonalizadaId: String? = null,
    val fecha: LocalDate = todayLocalDate(),
    val ingresoToEdit: IngresoModel? = null
) {
    fun reset() = copy(
        concepto = "",
        monto = "",
        categoriaSeleccionada = null,
        categoriaPersonalizadaId = null,
        fecha = todayLocalDate(),
        ingresoToEdit = null
    )
}
