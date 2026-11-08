package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.esAhorro
import bas.orellana.gastostracker.domain.usecase.AddIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.GetIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalesGastosPorCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateIngresoUseCase
import bas.orellana.gastostracker.domain.util.rangoDelMes
import bas.orellana.gastostracker.presentation.state.AddIngresoState
import bas.orellana.gastostracker.presentation.state.BalanceState
import bas.orellana.gastostracker.presentation.util.parseImporte
import bas.orellana.gastostracker.presentation.util.randomId
import bas.orellana.gastostracker.presentation.util.toPlainStringTrimmed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class BalanceViewModel(
    private val getIngresosUseCase: GetIngresosUseCase,
    private val addIngresoUseCase: AddIngresoUseCase,
    private val updateIngresoUseCase: UpdateIngresoUseCase,
    private val deleteIngresoUseCase: DeleteIngresoUseCase,
    private val getTotalIngresosUseCase: GetTotalIngresosUseCase,
    private val getTotalesGastosPorCategoriaUseCase: GetTotalesGastosPorCategoriaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BalanceState())
    val state: StateFlow<BalanceState> = _state.asStateFlow()

    private val _addIngresoState = MutableStateFlow(AddIngresoState())
    val addIngresoState: StateFlow<AddIngresoState> = _addIngresoState.asStateFlow()

    /** (año, mes) seleccionado en el Balance. */
    private val mesSeleccionado = MutableStateFlow(_state.value.selectedYear to _state.value.selectedMonth)

    init {
        loadIngresos()
        observarTotalesDelMes()
    }

    private fun loadIngresos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getIngresosUseCase()
                .catch { _state.update { it.copy(isLoading = false) } }
                .collect { ingresos ->
                    _state.update { it.copy(ingresos = ingresos, isLoading = false) }
                }
        }
    }

    /** Totales del mes seleccionado calculados en SQL; se recalculan al cambiar de mes o de datos. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarTotalesDelMes() {
        viewModelScope.launch {
            mesSeleccionado
                .flatMapLatest { (year, month) ->
                    val rango = rangoDelMes(year, month)
                    val desde = rango.start
                    val hasta = rango.endInclusive
                    combine(
                        getTotalIngresosUseCase(desde, hasta),
                        getTotalesGastosPorCategoriaUseCase(desde, hasta)
                    ) { totalIngresos, totalesGastos ->
                        val (ahorro, gastos) = totalesGastos.partition { it.esAhorro }
                        Triple(totalIngresos, gastos.sumOf { it.total }, ahorro.sumOf { it.total })
                    }
                }
                .catch { }
                .collect { (totalIngresos, totalGastos, totalAhorro) ->
                    _state.update {
                        it.copy(
                            totalIngresosMes = totalIngresos,
                            totalGastosMes = totalGastos,
                            totalAhorroMes = totalAhorro
                        )
                    }
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
            monto = ingreso.monto.toPlainStringTrimmed().replace('.', ','),
            categoriaSeleccionada = ingreso.categoria,
            categoriaPersonalizadaId = ingreso.categoriaPersonalizadaId,
            fecha = ingreso.fecha,
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
        mesSeleccionado.value = year to month
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

    fun updateFecha(fecha: LocalDate) {
        _addIngresoState.update { it.copy(fecha = fecha) }
    }

    fun saveIngreso(nombre: String, monto: String) {
        val montoDouble = parseImporte(monto) ?: return
        val ingresoToEdit = _addIngresoState.value.ingresoToEdit
        val fecha = _addIngresoState.value.fecha
        val ingreso = if (ingresoToEdit != null) {
            ingresoToEdit.copy(
                nombre = nombre,
                monto = montoDouble,
                fecha = fecha,
                categoria = _addIngresoState.value.categoriaSeleccionada,
                categoriaPersonalizadaId = _addIngresoState.value.categoriaPersonalizadaId
            )
        } else {
            IngresoModel(
                id = randomId(),
                nombre = nombre,
                monto = montoDouble,
                fecha = fecha,
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
        }
    }

    fun resetAddIngresoState() {
        _addIngresoState.value = AddIngresoState()
    }
}
