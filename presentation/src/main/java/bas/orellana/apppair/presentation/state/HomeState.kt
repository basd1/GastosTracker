package bas.orellana.apppair.presentation.state

import bas.orellana.apppair.domain.model.GastoModel

data class HomeState(
    val gastos: List<GastoModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)