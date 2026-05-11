package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.usecase.AddGastoUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.presentation.state.HomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class HomeViewModel(
    private val getGastosUseCase: GetGastosUseCase,
    private val addGastoUseCase: AddGastoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadGastos()
    }

    private fun loadGastos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getGastosUseCase()
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { gastos ->
                    _state.value = _state.value.copy(
                        gastos = gastos,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun showAddGastoDialog() {
        _state.update { it.copy(showAddGastoDialog = true) }
    }

    fun hideAddGastoDialog() {
        _state.update { it.copy(showAddGastoDialog = false) }
    }

    fun saveGasto(nombre: String, precio: String) {
        val monto = precio.toDoubleOrNull() ?: 0.0
        val gasto = GastoModel(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            monto = monto,
            fecha = LocalDate.now()
        )
        viewModelScope.launch {
            addGastoUseCase(gasto)
            _state.update { it.copy(gastos = listOf(gasto) + it.gastos, showAddGastoDialog = false) }
        }
    }
}