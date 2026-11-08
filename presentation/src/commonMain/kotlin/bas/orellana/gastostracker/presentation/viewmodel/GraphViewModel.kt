package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

/** Datos de la pantalla de gráficos: todos los gastos (los periodos se eligen en la propia pantalla). */
class GraphViewModel(
    getGastosUseCase: GetGastosUseCase,
    getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase
) : ViewModel() {

    val gastos: StateFlow<List<GastoModel>> = getGastosUseCase()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categoriasPersonalizadas: StateFlow<List<CategoriaPersonalizada>> = getCategoriasPersonalizadasUseCase()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
