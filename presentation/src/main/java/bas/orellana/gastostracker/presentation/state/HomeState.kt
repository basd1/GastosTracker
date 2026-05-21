package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.GastoModel

data class CategoryAlert(
    val categoriaNombre: String,
    val categoriaColor: Long,
    val gastoActual: Double,
    val gastoAnterior: Double
)

data class HomeState(
    val gastos: List<GastoModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddGastoDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showManageCategoriasDialog: Boolean = false,
    val alerts: List<CategoryAlert> = emptyList()
)