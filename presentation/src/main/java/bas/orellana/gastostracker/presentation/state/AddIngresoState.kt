package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.IngresoModel
import java.time.LocalDate

data class AddIngresoState(
    val concepto: String = "",
    val monto: String = "",
    val categoriaSeleccionada: Categoria? = null,
    val categoriaPersonalizadaId: String? = null,
    val fecha: LocalDate = LocalDate.now(),
    val ingresoToEdit: IngresoModel? = null
) {
    fun reset() = copy(
        concepto = "",
        monto = "",
        categoriaSeleccionada = null,
        categoriaPersonalizadaId = null,
        fecha = LocalDate.now(),
        ingresoToEdit = null
    )
}
