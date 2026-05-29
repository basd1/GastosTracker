package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.usecase.AddIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.usecase.GetIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateIngresoUseCase
import bas.orellana.gastostracker.presentation.state.AddIngresoState
import bas.orellana.gastostracker.presentation.state.BalanceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class BalanceViewModel(
    private val getIngresosUseCase: GetIngresosUseCase,
    private val addIngresoUseCase: AddIngresoUseCase,
    private val updateIngresoUseCase: UpdateIngresoUseCase,
    private val deleteIngresoUseCase: DeleteIngresoUseCase,
    private val getGastosUseCase: GetGastosUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BalanceState())
    val state: StateFlow<BalanceState> = _state.asStateFlow()

    private val _addIngresoState = MutableStateFlow(AddIngresoState())
    val addIngresoState: StateFlow<AddIngresoState> = _addIngresoState.asStateFlow()

    init {
        loadIngresos()
        loadGastos()
    }

    private fun loadIngresos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getIngresosUseCase()
                .catch { e ->
                    _state.value = _state.value.copy(isLoading = false)
                }
                .collect { ingresos ->
                    val selectedMonth = _state.value.selectedMonth
                    val selectedYear = _state.value.selectedYear
                    val totalIngresosMes = ingresos.filter {
                        it.fecha.monthValue == selectedMonth && it.fecha.year == selectedYear
                    }.sumOf { it.monto }
                    _state.value = _state.value.copy(
                        ingresos = ingresos,
                        totalIngresosMes = totalIngresosMes,
                        isLoading = false
                    )
                }
        }
    }

    private fun loadGastos() {
        viewModelScope.launch {
            getGastosUseCase()
                .catch { }
                .collect { gastos ->
                    val selectedMonth = _state.value.selectedMonth
                    val selectedYear = _state.value.selectedYear
                    val gastosDelMes = gastos.filter {
                        it.fecha.monthValue == selectedMonth && it.fecha.year == selectedYear
                    }
                    val gastosSinAhorro = gastosDelMes.filter { it.categoria != Categoria.AHORRO }
                    val ahorroDelMes = gastosDelMes.filter { it.categoria == Categoria.AHORRO }
                    val totalGastosMes = gastosSinAhorro.sumOf { it.monto }
                    val totalAhorroMes = ahorroDelMes.sumOf { it.monto }
                    _state.update { it.copy(totalGastosMes = totalGastosMes, totalAhorroMes = totalAhorroMes) }
                }
        }
    }

    fun showAddIngresoDialog() {
        _addIngresoState.value = AddIngresoState()
        _state.update { it.copy(showAddIngresoDialog = true) }
    }

    fun showEditIngresoDialog(ingreso: IngresoModel) {
        _addIngresoState.value = AddIngresoState(
            concepto = ingreso.nombre,
            monto = ingreso.monto.toBigDecimal().stripTrailingZeros().toPlainString(),
            categoriaSeleccionada = ingreso.categoria,
            categoriaPersonalizadaId = ingreso.categoriaPersonalizadaId,
            ingresoToEdit = ingreso
        )
        _state.update { it.copy(showAddIngresoDialog = true) }
    }

    fun hideAddIngresoDialog() {
        _state.update { it.copy(showAddIngresoDialog = false) }
    }

    fun showSettingsDialog() {
        _state.update { it.copy(showSettingsDialog = true) }
    }

    fun hideSettingsDialog() {
        _state.update { it.copy(showSettingsDialog = false) }
    }

    fun showMonthPicker() {
        _state.update { it.copy(showMonthPicker = true) }
    }

    fun hideMonthPicker() {
        _state.update { it.copy(showMonthPicker = false) }
    }

    fun updateSelectedMonth(month: Int, year: Int) {
        _state.update {
            it.copy(selectedMonth = month, selectedYear = year, showMonthPicker = false)
        }
        loadIngresos()
        loadGastos()
    }

    fun updateConcepto(concepto: String) {
        _addIngresoState.update { it.copy(concepto = concepto) }
    }

    fun updateMonto(monto: String) {
        _addIngresoState.update { it.copy(monto = monto) }
    }

    fun updateCategoria(categoria: Categoria?) {
        _addIngresoState.update { it.copy(categoriaSeleccionada = categoria) }
    }

    fun updateCategoriaPersonalizada(id: String?) {
        _addIngresoState.update { it.copy(categoriaPersonalizadaId = id) }
    }

    fun saveIngreso(nombre: String, monto: String) {
        val montoDouble = monto.toDoubleOrNull() ?: 0.0
        val ingresoToEdit = _addIngresoState.value.ingresoToEdit
        val ingreso = if (ingresoToEdit != null) {
            ingresoToEdit.copy(
                nombre = nombre,
                monto = montoDouble,
                categoria = _addIngresoState.value.categoriaSeleccionada,
                categoriaPersonalizadaId = _addIngresoState.value.categoriaPersonalizadaId
            )
        } else {
            IngresoModel(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                monto = montoDouble,
                fecha = LocalDate.now(),
                categoria = _addIngresoState.value.categoriaSeleccionada,
                categoriaPersonalizadaId = _addIngresoState.value.categoriaPersonalizadaId
            )
        }
        viewModelScope.launch {
            if (ingresoToEdit != null) {
                updateIngresoUseCase(ingreso)
            } else {
                addIngresoUseCase(ingreso)
            }
            _state.update { it.copy(showAddIngresoDialog = false) }
            loadIngresos()
        }
    }

    fun deleteIngreso(id: String) {
        viewModelScope.launch {
            deleteIngresoUseCase(id)
            _state.update { state -> state.copy(ingresos = state.ingresos.filter { it.id != id }) }
        }
    }

    fun restoreIngreso(ingreso: IngresoModel) {
        viewModelScope.launch {
            addIngresoUseCase(ingreso)
            _state.update { state -> state.copy(ingresos = state.ingresos + ingreso) }
            loadIngresos()
        }
    }

    fun resetAddIngresoState() {
        _addIngresoState.value = AddIngresoState()
    }
}
