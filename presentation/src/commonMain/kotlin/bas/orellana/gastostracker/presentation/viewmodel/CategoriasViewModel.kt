package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.ContarMovimientosCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.presentation.state.ManageCategoriasState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Categorías personalizadas: la lista y el diálogo para crearlas, editarlas y borrarlas
 * (preguntando a dónde pasar sus movimientos si tiene).
 */
class CategoriasViewModel(
    getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase,
    private val addCategoriaPersonalizadaUseCase: AddCategoriaPersonalizadaUseCase,
    private val updateCategoriaPersonalizadaUseCase: UpdateCategoriaPersonalizadaUseCase,
    private val deleteCategoriaPersonalizadaUseCase: DeleteCategoriaPersonalizadaUseCase,
    private val contarMovimientosCategoriaUseCase: ContarMovimientosCategoriaUseCase
) : ViewModel() {

    val categoriasPersonalizadas: StateFlow<List<CategoriaPersonalizada>> = getCategoriasPersonalizadasUseCase()
        .catch { }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _state = MutableStateFlow(ManageCategoriasState())
    val state: StateFlow<ManageCategoriasState> = _state.asStateFlow()

    fun mostrarDialogo() = _state.update { ManageCategoriasState(visible = true) }

    fun ocultarDialogo() = _state.update { ManageCategoriasState() }

    fun updateNuevaCategoria(nombre: String) = _state.update { it.copy(nuevaCategoria = nombre) }

    fun updateColorSeleccionado(color: Long) = _state.update { it.copy(colorSeleccionado = color) }

    fun addCategoria() {
        val state = _state.value
        val nombre = state.nuevaCategoria.trim()
        if (nombre.isEmpty()) return
        _state.update { it.copy(nuevaCategoria = "", colorSeleccionado = ManageCategoriasState().colorSeleccionado) }
        viewModelScope.launch { addCategoriaPersonalizadaUseCase(nombre, state.colorSeleccionado) }
    }

    fun showEditCategoriaDialog(categoria: CategoriaPersonalizada) {
        _state.update { it.copy(editCategoria = categoria, editNombre = categoria.nombre, editColor = categoria.color) }
    }

    fun updateEditNombre(nombre: String) = _state.update { it.copy(editNombre = nombre) }

    fun updateEditColor(color: Long) = _state.update { it.copy(editColor = color) }

    fun saveEditCategoria() {
        val state = _state.value
        val categoria = state.editCategoria ?: return
        val editada = categoria.copy(nombre = state.editNombre, color = state.editColor)
        cancelEditCategoria()
        viewModelScope.launch { updateCategoriaPersonalizadaUseCase(editada) }
    }

    fun cancelEditCategoria() {
        val vacio = ManageCategoriasState()
        _state.update { it.copy(editCategoria = null, editNombre = vacio.editNombre, editColor = vacio.editColor) }
    }

    /**
     * Si la categoría no tiene movimientos se borra directamente; si tiene, se pregunta antes
     * a qué categoría pasarlos (ver [confirmarEliminarCategoria]).
     */
    fun solicitarEliminarCategoria(categoria: CategoriaPersonalizada) {
        viewModelScope.launch {
            val movimientos = contarMovimientosCategoriaUseCase(categoria.id)
            if (movimientos == 0) {
                eliminarCategoria(categoria.id, CategoriaDestino.POR_DEFECTO)
            } else {
                _state.update {
                    it.copy(
                        categoriaAEliminar = categoria,
                        movimientosCategoriaAEliminar = movimientos,
                        destinoReasignacion = CategoriaDestino.POR_DEFECTO
                    )
                }
            }
        }
    }

    fun updateDestinoReasignacion(destino: CategoriaDestino) = _state.update { it.copy(destinoReasignacion = destino) }

    fun confirmarEliminarCategoria() {
        val state = _state.value
        val categoria = state.categoriaAEliminar ?: return
        cancelarEliminarCategoria()
        viewModelScope.launch { eliminarCategoria(categoria.id, state.destinoReasignacion) }
    }

    fun cancelarEliminarCategoria() {
        _state.update {
            it.copy(categoriaAEliminar = null, movimientosCategoriaAEliminar = 0, destinoReasignacion = CategoriaDestino.POR_DEFECTO)
        }
    }

    private suspend fun eliminarCategoria(id: String, destino: CategoriaDestino) {
        deleteCategoriaPersonalizadaUseCase(id, destino)
        if (_state.value.editCategoria?.id == id) cancelEditCategoria()
    }
}
