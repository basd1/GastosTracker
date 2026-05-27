package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.IngresoModel

data class AddIngresoState(
    val concepto: String = "",
    val monto: String = "",
    val categoriaSeleccionada: Categoria? = null,
    val categoriaPersonalizadaId: String? = null,
    val ingresoToEdit: IngresoModel? = null
) {
    fun reset() = copy(
        concepto = "",
        monto = "",
        categoriaSeleccionada = null,
        categoriaPersonalizadaId = null,
        ingresoToEdit = null
    )
}
