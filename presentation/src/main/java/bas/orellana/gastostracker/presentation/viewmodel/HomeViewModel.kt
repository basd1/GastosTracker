package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.AddGastoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteGastoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.presentation.state.AddGastoState
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
    private val deleteGastoUseCase: DeleteGastoUseCase,
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
                    _state.value = _state.value.copy(
                        gastos = gastos,
                        isLoading = false,
                        error = null
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
                }
        }
    }

    fun showAddGastoDialog() {
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
        val gasto = GastoModel(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            monto = monto,
            fecha = LocalDate.now(),
            categoria = categoria,
            categoriaPersonalizadaId = categoriaPersonalizadaId
        )
        viewModelScope.launch {
            addGastoUseCase(gasto)
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
}