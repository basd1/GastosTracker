package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.IngresoModel

data class AddIngresoState(
    val concepto: String = "",
    val monto: String = "",
    val ingresoToEdit: IngresoModel? = null
) {
    fun reset() = copy(
        concepto = "",
        monto = "",
        ingresoToEdit = null
    )
}
