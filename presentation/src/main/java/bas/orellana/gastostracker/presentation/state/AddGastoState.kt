package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel

data class AddGastoState(
    val concepto: String = "",
    val precio: String = "",
    val categoriaSeleccionada: Categoria? = null,
    val categoriaPersonalizadaSeleccionada: String? = null,
    val gastoToEdit: GastoModel? = null
) {
    fun reset() = copy(
        concepto = "",
        precio = "",
        categoriaSeleccionada = null,
        categoriaPersonalizadaSeleccionada = null,
        gastoToEdit = null
    )
}