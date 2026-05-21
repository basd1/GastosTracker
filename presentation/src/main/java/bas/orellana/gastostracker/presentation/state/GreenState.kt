package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.IngresoModel

data class GreenState(
    val ingresos: List<IngresoModel> = emptyList(),
    val isLoading: Boolean = false,
    val showAddIngresoDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val totalIngresosMes: Double = 0.0,
    val totalGastosMes: Double = 0.0
)
