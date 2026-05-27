package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.AddGastoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteAllGastosUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteGastoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateGastoUseCase
import bas.orellana.gastostracker.presentation.state.AddGastoState
import bas.orellana.gastostracker.presentation.state.CategoryAlert
import bas.orellana.gastostracker.presentation.state.HomeState
import bas.orellana.gastostracker.presentation.state.ManageCategoriasState
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
    private val addGastoUseCase: AddGastoUseCase,
    private val updateGastoUseCase: UpdateGastoUseCase,
    private val deleteGastoUseCase: DeleteGastoUseCase,
    private val deleteAllGastosUseCase: DeleteAllGastosUseCase,
    private val getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase,
    private val addCategoriaPersonalizadaUseCase: AddCategoriaPersonalizadaUseCase,
    private val deleteCategoriaPersonalizadaUseCase: DeleteCategoriaPersonalizadaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _categoriasPersonalizadas = MutableStateFlow<List<CategoriaPersonalizada>>(emptyList())
    val categoriasPersonalizadas: StateFlow<List<CategoriaPersonalizada>> = _categoriasPersonalizadas.asStateFlow()

    private val _addGastoState = MutableStateFlow(AddGastoState())
    val addGastoState: StateFlow<AddGastoState> = _addGastoState.asStateFlow()

    private val _manageCategoriasState = MutableStateFlow(ManageCategoriasState())
    val manageCategoriasState: StateFlow<ManageCategoriasState> = _manageCategoriasState.asStateFlow()

    init {
        loadGastos()
        loadCategoriasPersonalizadas()
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
                    val allAlerts = calculateAlerts(gastos, _categoriasPersonalizadas.value)
                    val alerts = allAlerts.filter { it.categoriaNombre !in _state.value.dismissedAlertKeys }
                    _state.value = _state.value.copy(
                        gastos = gastos,
                        isLoading = false,
                        error = null,
                        alerts = alerts
                    )
                }
        }
    }

    private fun loadCategoriasPersonalizadas() {
        viewModelScope.launch {
            getCategoriasPersonalizadasUseCase()
                .catch { }
                .collect { categorias ->
                    _categoriasPersonalizadas.value = categorias
                    val currentGastos = _state.value.gastos
                    val allAlerts = calculateAlerts(currentGastos, categorias)
                    val alerts = allAlerts.filter { it.categoriaNombre !in _state.value.dismissedAlertKeys }
                    _state.update { it.copy(alerts = alerts) }
                }
        }
    }

    fun showAddGastoDialog() {
        _addGastoState.value = AddGastoState()
        _state.update { it.copy(showAddGastoDialog = true) }
    }

    fun showEditGastoDialog(gasto: GastoModel) {
        _addGastoState.value = AddGastoState(
            concepto = gasto.nombre,
            precio = gasto.monto.toBigDecimal().stripTrailingZeros().toPlainString(),
            categoriaSeleccionada = gasto.categoria,
            categoriaPersonalizadaSeleccionada = gasto.categoriaPersonalizadaId,
            fecha = gasto.fecha,
            gastoToEdit = gasto
        )
        _state.update { it.copy(showAddGastoDialog = true) }
    }

    fun hideAddGastoDialog() {
        _state.update { it.copy(showAddGastoDialog = false) }
    }

    fun showSettingsDialog() {
        _state.update { it.copy(showSettingsDialog = true) }
    }

    fun hideSettingsDialog() {
        _state.update { it.copy(showSettingsDialog = false) }
    }

    fun showManageCategoriasDialog() {
        _state.update { it.copy(showManageCategoriasDialog = true, showSettingsDialog = false) }
    }

    fun hideManageCategoriasDialog() {
        _state.update { it.copy(showManageCategoriasDialog = false) }
    }

    fun saveGasto(nombre: String, precio: String, categoria: Categoria?, categoriaPersonalizadaId: String?) {
        val monto = precio.toDoubleOrNull() ?: 0.0
        val fecha = _addGastoState.value.fecha
        val gastoToEdit = _addGastoState.value.gastoToEdit
        val gasto = if (gastoToEdit != null) {
            gastoToEdit.copy(
                nombre = nombre,
                monto = monto,
                fecha = fecha,
                categoria = categoria,
                categoriaPersonalizadaId = categoriaPersonalizadaId
            )
        } else {
            GastoModel(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                monto = monto,
                fecha = fecha,
                categoria = categoria,
                categoriaPersonalizadaId = categoriaPersonalizadaId
            )
        }
        viewModelScope.launch {
            if (gastoToEdit != null) {
                updateGastoUseCase(gasto)
            } else {
                addGastoUseCase(gasto)
            }
            _state.update { it.copy(showAddGastoDialog = false) }
            loadGastos()
        }
    }

    fun deleteGasto(id: String) {
        viewModelScope.launch {
            deleteGastoUseCase(id)
            _state.update { state -> state.copy(gastos = state.gastos.filter { it.id != id }) }
        }
    }

    fun seedTestAhorroData() {
        viewModelScope.launch {
            val montos = listOf(200.0, 350.0, 150.0, 400.0, 250.0, 300.0, 180.0, 420.0, 310.0, 275.0, 500.0, 380.0)
            val now = LocalDate.now()
            montos.forEachIndexed { index, monto ->
                val gasto = GastoModel(
                    id = UUID.randomUUID().toString(),
                    nombre = "Ahorro ${index + 1}",
                    monto = monto,
                    fecha = now.minusMonths(11 - index.toLong()),
                    categoria = Categoria.AHORRO,
                    categoriaPersonalizadaId = null
                )
                addGastoUseCase(gasto)
            }
            loadGastos()
        }
    }

    fun seedTestAhorro45Meses() {
        viewModelScope.launch {
            val now = LocalDate.now()
            (0 until 45).forEach { i ->
                val monto = (100..600).random().toDouble()
                val gasto = GastoModel(
                    id = UUID.randomUUID().toString(),
                    nombre = "Ahorro ${i + 1}",
                    monto = monto,
                    fecha = now.minusMonths(44 - i.toLong()),
                    categoria = Categoria.AHORRO,
                    categoriaPersonalizadaId = null
                )
                addGastoUseCase(gasto)
            }
            loadGastos()
        }
    }

    fun deleteAllGastos() {
        viewModelScope.launch {
            deleteAllGastosUseCase()
            loadGastos()
        }
    }

    fun deleteAllAhorros() {
        viewModelScope.launch {
            val ahorros = _state.value.gastos.filter { it.categoria == Categoria.AHORRO }
            ahorros.forEach { deleteGastoUseCase(it.id) }
            loadGastos()
        }
    }

    fun addCategoriaPersonalizada(nombre: String, color: Long) {
        viewModelScope.launch {
            addCategoriaPersonalizadaUseCase(nombre, color)
        }
    }

    fun deleteCategoriaPersonalizada(id: String) {
        viewModelScope.launch {
            deleteCategoriaPersonalizadaUseCase(id)
            _categoriasPersonalizadas.value = _categoriasPersonalizadas.value.filter { it.id != id }
        }
    }

    fun updateAddGastoConcepto(concepto: String) {
        _addGastoState.update { it.copy(concepto = concepto) }
    }

    fun updateAddGastoPrecio(precio: String) {
        _addGastoState.update { it.copy(precio = precio) }
    }

    fun updateAddGastoCategoria(categoria: Categoria?) {
        _addGastoState.update { it.copy(categoriaSeleccionada = categoria, categoriaPersonalizadaSeleccionada = null) }
    }

    fun updateAddGastoCategoriaPersonalizada(id: String?) {
        _addGastoState.update { current: AddGastoState ->
            current.copy(categoriaPersonalizadaSeleccionada = id, categoriaSeleccionada = null)
        }
    }

    fun updateAddGastoFecha(fecha: LocalDate) {
        _addGastoState.update { it.copy(fecha = fecha) }
    }

    fun resetAddGastoState() {
        _addGastoState.value = AddGastoState()
    }

    fun updateNuevaCategoria(nombre: String) {
        _manageCategoriasState.update { current: ManageCategoriasState ->
            current.copy(nuevaCategoria = nombre)
        }
    }

    fun updateColorSeleccionado(color: Long) {
        _manageCategoriasState.update { current: ManageCategoriasState ->
            current.copy(colorSeleccionado = color)
        }
    }

    fun resetManageCategoriasState() {
        _manageCategoriasState.value = ManageCategoriasState()
    }

    fun dismissAlert(categoriaNombre: String) {
        _state.update {
            it.copy(
                dismissedAlertKeys = it.dismissedAlertKeys + categoriaNombre,
                alerts = it.alerts.filter { a -> a.categoriaNombre != categoriaNombre }
            )
        }
    }

    private fun calculateAlerts(
        gastos: List<GastoModel>,
        categoriasPersonalizadas: List<CategoriaPersonalizada>
    ): List<CategoryAlert> {
        val now = LocalDate.now()
        val currentMonth = gastos.filter {
            it.fecha.year == now.year && it.fecha.month == now.month && it.categoria != Categoria.AHORRO
        }
        val previousMonth = now.minusMonths(1)
        val previousMonthGastos = gastos.filter {
            it.fecha.year == previousMonth.year && it.fecha.month == previousMonth.month && it.categoria != Categoria.AHORRO
        }

        val currentByCategory = mutableMapOf<String, MutableList<GastoModel>>()
        currentMonth.forEach { gasto ->
            val cat = gasto.categoria
            val catPersonalizadaId = gasto.categoriaPersonalizadaId
            val key = when {
                cat != null -> "enum:${cat.name}"
                catPersonalizadaId != null -> "custom:$catPersonalizadaId"
                else -> "sin_categoria"
            }
            currentByCategory.getOrPut(key) { mutableListOf() }.add(gasto)
        }

        val previousByCategory = mutableMapOf<String, MutableList<GastoModel>>()
        previousMonthGastos.forEach { gasto ->
            val cat = gasto.categoria
            val catPersonalizadaId = gasto.categoriaPersonalizadaId
            val key = when {
                cat != null -> "enum:${cat.name}"
                catPersonalizadaId != null -> "custom:$catPersonalizadaId"
                else -> "sin_categoria"
            }
            previousByCategory.getOrPut(key) { mutableListOf() }.add(gasto)
        }

        val alerts = mutableListOf<CategoryAlert>()
        currentByCategory.forEach { (key, currentGastos) ->
            val currentTotal = currentGastos.sumOf { it.monto }
            val previousTotal = previousByCategory[key]?.sumOf { it.monto } ?: 0.0

            if (previousTotal > 0 && currentTotal > previousTotal) {
                val (nombre, color) = when {
                    key.startsWith("enum:") -> {
                        val enumName = key.removePrefix("enum:")
                        val cat = Categoria.entries.find { it.name == enumName }
                        cat?.displayName to (cat?.color ?: 0xFF607D8B)
                    }
                    key.startsWith("custom:") -> {
                        val customId = key.removePrefix("custom:")
                        val cat = categoriasPersonalizadas.find { it.id == customId }
                        cat?.nombre to (cat?.color ?: 0xFF607D8B)
                    }
                    else -> "Sin categoría" to 0xFF607D8B
                }
                alerts.add(
                    CategoryAlert(
                        categoriaNombre = nombre ?: "Sin categoría",
                        categoriaColor = color ?: 0xFF607D8B,
                        gastoActual = currentTotal,
                        gastoAnterior = previousTotal
                    )
                )
            }
        }

        return alerts
    }
}