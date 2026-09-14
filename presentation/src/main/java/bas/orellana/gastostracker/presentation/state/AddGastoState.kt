package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.datetime.LocalDate

data class AddGastoState(
    val concepto: String = "",
    val precio: String = "",
    val categoriaSeleccionada: Categoria? = null,
    val categoriaPersonalizadaSeleccionada: String? = null,
    val fecha: LocalDate = todayLocalDate(),
    val gastoToEdit: GastoModel? = null
) {
    fun reset() = copy(
        concepto = "",
        precio = "",
        categoriaSeleccionada = null,
        categoriaPersonalizadaSeleccionada = null,
        fecha = todayLocalDate(),
        gastoToEdit = null
    )
}