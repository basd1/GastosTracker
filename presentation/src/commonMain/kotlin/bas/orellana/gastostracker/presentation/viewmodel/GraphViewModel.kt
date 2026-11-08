package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.usecase.GetTodasLasAportacionesUseCase
import bas.orellana.gastostracker.domain.usecase.GetIngresosUseCase
import bas.orellana.gastostracker.domain.model.IngresoModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

/** Datos de la pantalla de gráficos: todos los gastos (los periodos se eligen en la propia pantalla). */
class GraphViewModel(
    getGastosUseCase: GetGastosUseCase,
    getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase,
    getTodasLasAportacionesUseCase: GetTodasLasAportacionesUseCase,
    getIngresosUseCase: GetIngresosUseCase
) : ViewModel() {

    val gastos: StateFlow<List<GastoModel>> = getGastosUseCase()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categoriasPersonalizadas: StateFlow<List<CategoriaPersonalizada>> = getCategoriasPersonalizadasUseCase()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Aportaciones a metas de ahorro, de la más antigua a la más reciente. */
    val aportaciones: StateFlow<List<Aportacion>> = getTodasLasAportacionesUseCase()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Todos los ingresos (para comparar ingresos y gastos mes a mes). */
    val ingresos: StateFlow<List<IngresoModel>> = getIngresosUseCase()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
