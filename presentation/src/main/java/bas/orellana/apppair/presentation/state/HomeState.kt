package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.GastoModel

data class HomeState(
    val gastos: List<GastoModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddGastoDialog: Boolean = false
)