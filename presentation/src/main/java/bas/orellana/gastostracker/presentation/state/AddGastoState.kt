package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import java.time.LocalDate

data class AddGastoState(
    val concepto: String = "",
    val precio: String = "",
    val categoriaSeleccionada: Categoria? = null,
    val categoriaPersonalizadaSeleccionada: String? = null,
    val fecha: LocalDate = LocalDate.now(),
    val gastoToEdit: GastoModel? = null
) {
    fun reset() = copy(
        concepto = "",
        precio = "",
        categoriaSeleccionada = null,
        categoriaPersonalizadaSeleccionada = null,
        fecha = LocalDate.now(),
        gastoToEdit = null
    )
}