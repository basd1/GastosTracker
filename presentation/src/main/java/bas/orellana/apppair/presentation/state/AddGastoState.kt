package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria

data class AddGastoState(
    val concepto: String = "",
    val precio: String = "",
    val categoriaSeleccionada: Categoria? = null,
    val categoriaPersonalizadaSeleccionada: String? = null
) {
    fun reset() = copy(
        concepto = "",
        precio = "",
        categoriaSeleccionada = null,
        categoriaPersonalizadaSeleccionada = null
    )
}