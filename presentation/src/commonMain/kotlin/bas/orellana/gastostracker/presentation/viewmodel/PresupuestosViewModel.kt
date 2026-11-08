package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.EstadoPresupuesto
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.domain.usecase.EliminarPresupuestoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetEstadoPresupuestosUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarPresupuestoUseCase
import bas.orellana.gastostracker.presentation.util.parseImporte
import bas.orellana.gastostracker.presentation.util.toPlainStringTrimmed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Presupuesto que se está creando o editando en el diálogo. */
data class EditorPresupuesto(
    val categoria: Categoria? = null,
    val categoriaPersonalizadaId: String? = null,
    val monto: String = "",
    /** Al editar no se puede cambiar la categoría (sería otro presupuesto). */
    val esNuevo: Boolean = true
) {
    val clave: String get() = Presupuesto.claveDe(categoria, categoriaPersonalizadaId)
    val importe: Double? get() = parseImporte(monto)
}

data class PresupuestosState(
    val mes: Mes = Mes.actual(),
    val estados: List<EstadoPresupuesto> = emptyList(),
    val categoriasPersonalizadas: List<CategoriaPersonalizada> = emptyList(),
    val editor: EditorPresupuesto? = null,
    val showMonthPicker: Boolean = false
) {
    private val clavesUsadas: Set<String> get() = estados.map { it.presupuesto.clave }.toSet()

    /** Categorías que todavía no tienen presupuesto (para el diálogo de nuevo presupuesto). */
    val globalDisponible: Boolean get() = Presupuesto.CLAVE_GLOBAL !in clavesUsadas
    val categoriasDisponibles: List<Categoria>
        get() = Categoria.entries.filter { Presupuesto.claveDe(it, null) !in clavesUsadas }
    val personalizadasDisponibles: List<CategoriaPersonalizada>
        get() = categoriasPersonalizadas.filter { Presupuesto.claveDe(null, it.id) !in clavesUsadas }
    val hayCategoriasDisponibles: Boolean
        get() = globalDisponible || categoriasDisponibles.isNotEmpty() || personalizadasDisponibles.isNotEmpty()
}

class PresupuestosViewModel(
    private val getEstadoPresupuestosUseCase: GetEstadoPresupuestosUseCase,
    private val guardarPresupuestoUseCase: GuardarPresupuestoUseCase,
    private val eliminarPresupuestoUseCase: EliminarPresupuestoUseCase,
    getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PresupuestosState())
    val state: StateFlow<PresupuestosState> = _state.asStateFlow()

    private val mesSeleccionado = MutableStateFlow(_state.value.mes)

    init {
        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            mesSeleccionado
                .flatMapLatest { mes -> getEstadoPresupuestosUseCase(mes.primerDia, mes.ultimoDia) }
                .catch { }
                .collect { estados -> _state.update { it.copy(estados = estados) } }
        }
        viewModelScope.launch {
            getCategoriasPersonalizadasUseCase()
                .catch { }
                .collect { categorias -> _state.update { it.copy(categoriasPersonalizadas = categorias) } }
        }
    }

    fun seleccionarMes(mes: Mes) {
        mesSeleccionado.value = mes
        _state.update { it.copy(mes = mes, showMonthPicker = false) }
    }

    fun mesAnterior() = seleccionarMes(mesSeleccionado.value.anterior())

    fun mesSiguiente() = seleccionarMes(mesSeleccionado.value.siguiente())

    fun showMonthPicker() = _state.update { it.copy(showMonthPicker = true) }

    fun hideMonthPicker() = _state.update { it.copy(showMonthPicker = false) }

    /** Abre el diálogo con la primera categoría libre ya elegida (el global si no existe). */
    fun nuevoPresupuesto() {
        val state = _state.value
        val editor = when {
            state.globalDisponible -> EditorPresupuesto()
            state.categoriasDisponibles.isNotEmpty() -> EditorPresupuesto(categoria = state.categoriasDisponibles.first())
            state.personalizadasDisponibles.isNotEmpty() ->
                EditorPresupuesto(categoriaPersonalizadaId = state.personalizadasDisponibles.first().id)
            else -> return
        }
        _state.update { it.copy(editor = editor) }
    }

    fun editar(presupuesto: Presupuesto) {
        _state.update {
            it.copy(
                editor = EditorPresupuesto(
                    categoria = presupuesto.categoria,
                    categoriaPersonalizadaId = presupuesto.categoriaPersonalizadaId,
                    monto = presupuesto.monto.toPlainStringTrimmed().replace('.', ','),
                    esNuevo = false
                )
            )
        }
    }

    fun elegirCategoria(categoria: Categoria?, categoriaPersonalizadaId: String?) {
        _state.update { state ->
            val editor = state.editor?.takeIf { it.esNuevo } ?: return@update state
            state.copy(editor = editor.copy(categoria = categoria, categoriaPersonalizadaId = categoriaPersonalizadaId))
        }
    }

    fun cambiarMonto(monto: String) {
        _state.update { state -> state.copy(editor = state.editor?.copy(monto = monto)) }
    }

    fun cancelarEdicion() = _state.update { it.copy(editor = null) }

    fun guardar() {
        val editor = _state.value.editor ?: return
        val importe = editor.importe ?: return
        _state.update { it.copy(editor = null) }
        viewModelScope.launch {
            guardarPresupuestoUseCase(Presupuesto(editor.categoria, editor.categoriaPersonalizadaId, importe))
        }
    }

    fun eliminarEditado() {
        val editor = _state.value.editor ?: return
        _state.update { it.copy(editor = null) }
        viewModelScope.launch { eliminarPresupuestoUseCase(editor.clave) }
    }
}
