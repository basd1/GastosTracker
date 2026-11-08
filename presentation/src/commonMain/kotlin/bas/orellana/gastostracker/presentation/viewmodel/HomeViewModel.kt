package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.TotalPorCategoria
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.AddGastoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteAllGastosUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteGastoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalesGastosPorCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateGastoUseCase
import bas.orellana.gastostracker.domain.util.rangoDelMes
import bas.orellana.gastostracker.domain.util.todayLocalDate
import bas.orellana.gastostracker.presentation.state.AddGastoState
import bas.orellana.gastostracker.presentation.state.CategoryAlert
import bas.orellana.gastostracker.presentation.state.HomeState
import bas.orellana.gastostracker.presentation.state.ManageCategoriasState
import bas.orellana.gastostracker.presentation.state.SortOrder
import bas.orellana.gastostracker.presentation.util.parseImporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import bas.orellana.gastostracker.presentation.util.randomId
import bas.orellana.gastostracker.presentation.util.toPlainStringTrimmed

class HomeViewModel(
    private val getGastosUseCase: GetGastosUseCase,
    private val addGastoUseCase: AddGastoUseCase,
    private val updateGastoUseCase: UpdateGastoUseCase,
    private val deleteGastoUseCase: DeleteGastoUseCase,
    private val deleteAllGastosUseCase: DeleteAllGastosUseCase,
    private val getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase,
    private val addCategoriaPersonalizadaUseCase: AddCategoriaPersonalizadaUseCase,
    private val updateCategoriaPersonalizadaUseCase: UpdateCategoriaPersonalizadaUseCase,
    private val deleteCategoriaPersonalizadaUseCase: DeleteCategoriaPersonalizadaUseCase,
    private val getTotalesGastosPorCategoriaUseCase: GetTotalesGastosPorCategoriaUseCase
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
        observarAlertas()
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
                    _state.update {
                        it.copy(
                            gastos = gastos,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadCategoriasPersonalizadas() {
        viewModelScope.launch {
            getCategoriasPersonalizadasUseCase()
                .catch { }
                .collect { categorias ->
                    _categoriasPersonalizadas.value = categorias
                }
        }
    }

    /** Compara lo gastado por categoría este mes con el mes anterior, sumando en SQL. */
    private fun observarAlertas() {
        val hoy = todayLocalDate()
        val mesAnterior = hoy.minus(1, DateTimeUnit.MONTH)
        val rangoActual = rangoDelMes(hoy.year, hoy.monthNumber)
        val rangoAnterior = rangoDelMes(mesAnterior.year, mesAnterior.monthNumber)
        viewModelScope.launch {
            combine(
                getTotalesGastosPorCategoriaUseCase(rangoActual.start, rangoActual.endInclusive),
                getTotalesGastosPorCategoriaUseCase(rangoAnterior.start, rangoAnterior.endInclusive),
                getCategoriasPersonalizadasUseCase()
            ) { actual, anterior, categorias -> calculateAlerts(actual, anterior, categorias) }
                .catch { }
                .collect { alerts ->
                    _state.update { state ->
                        state.copy(alerts = alerts.filter { it.categoriaNombre !in state.dismissedAlertKeys })
                    }
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
            precio = gasto.monto.toPlainStringTrimmed().replace('.', ','),
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
        val monto = parseImporte(precio) ?: return
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
                id = randomId(),
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
        }
    }

    fun deleteGasto(id: String) {
        viewModelScope.launch {
            deleteGastoUseCase(id)
            _state.update { state -> state.copy(gastos = state.gastos.filter { it.id != id }) }
        }
    }

    fun restoreGasto(gasto: GastoModel) {
        viewModelScope.launch {
            addGastoUseCase(gasto)
        }
    }

    fun deleteAllGastos() {
        viewModelScope.launch {
            deleteAllGastosUseCase()
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

    fun showEditCategoriaDialog(categoria: CategoriaPersonalizada) {
        _manageCategoriasState.update {
            it.copy(
                editCategoria = categoria,
                editNombre = categoria.nombre,
                editColor = categoria.color
            )
        }
    }

    fun updateEditNombre(nombre: String) {
        _manageCategoriasState.update { it.copy(editNombre = nombre) }
    }

    fun updateEditColor(color: Long) {
        _manageCategoriasState.update { it.copy(editColor = color) }
    }

    fun saveEditCategoria() {
        val state = _manageCategoriasState.value
        val categoria = state.editCategoria ?: return
        val updated = categoria.copy(
            nombre = state.editNombre,
            color = state.editColor
        )
        viewModelScope.launch {
            updateCategoriaPersonalizadaUseCase(updated)
            _categoriasPersonalizadas.value = _categoriasPersonalizadas.value.map {
                if (it.id == updated.id) updated else it
            }
            _manageCategoriasState.update {
                it.copy(editCategoria = null, editNombre = "", editColor = Categoria.PALETA_COLORES_PERSONALIZADOS.first())
            }
        }
    }

    fun cancelEditCategoria() {
        _manageCategoriasState.update {
            it.copy(editCategoria = null, editNombre = "", editColor = Categoria.PALETA_COLORES_PERSONALIZADOS.first())
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

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun updateFilterCategoria(categoria: Categoria?) {
        _state.update { it.copy(filterCategoria = categoria, filterCategoriaPersonalizadaId = null) }
    }

    fun updateFilterCategoriaPersonalizada(id: String?) {
        _state.update { it.copy(filterCategoriaPersonalizadaId = id, filterCategoria = null) }
    }

    fun clearFilters() {
        _state.update { it.copy(filterCategoria = null, filterCategoriaPersonalizadaId = null, searchQuery = "") }
    }

    fun updateSortOrder(order: SortOrder) {
        _state.update { it.copy(sortOrder = order) }
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
        totalesMesActual: List<TotalPorCategoria>,
        totalesMesAnterior: List<TotalPorCategoria>,
        categoriasPersonalizadas: List<CategoriaPersonalizada>
    ): List<CategoryAlert> {
        val anteriorPorCategoria = totalesMesAnterior
            .filter { it.categoria != Categoria.AHORRO }
            .associate { (it.categoria to it.categoriaPersonalizadaId) to it.total }

        return totalesMesActual
            .filter { it.categoria != Categoria.AHORRO }
            .mapNotNull { actual ->
                val totalAnterior = anteriorPorCategoria[actual.categoria to actual.categoriaPersonalizadaId] ?: 0.0
                if (totalAnterior <= 0 || actual.total <= totalAnterior) return@mapNotNull null

                val categoria = actual.categoria
                val personalizada = actual.categoriaPersonalizadaId?.let { id ->
                    categoriasPersonalizadas.find { it.id == id }
                }
                CategoryAlert(
                    categoriaNombre = categoria?.displayName ?: personalizada?.nombre ?: "Sin categor\u00EDa",
                    categoriaColor = categoria?.color ?: personalizada?.color ?: 0xFF607D8B,
                    gastoActual = actual.total,
                    gastoAnterior = totalAnterior
                )
            }
    }
}