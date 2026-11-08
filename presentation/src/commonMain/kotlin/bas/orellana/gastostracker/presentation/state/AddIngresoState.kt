package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.CategoriaIngreso
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.datetime.LocalDate

data class AddIngresoState(
    val concepto: String = "",
    val monto: String = "",
    val categoriaSeleccionada: CategoriaIngreso? = null,
    val categoriaPersonalizadaId: String? = null,
    val fecha: LocalDate = todayLocalDate(),
    val ingresoToEdit: IngresoModel? = null,
    /** Al crear: guardar como ingreso fijo que se repite cada mes. */
    val repetirMensual: Boolean = false,
    val nota: String = "",
    val cuentaId: String? = null
) {
    fun reset() = copy(
        concepto = "",
        monto = "",
        categoriaSeleccionada = null,
        categoriaPersonalizadaId = null,
        fecha = todayLocalDate(),
        ingresoToEdit = null,
        repetirMensual = false,
        nota = "",
        cuentaId = null
    )
}
