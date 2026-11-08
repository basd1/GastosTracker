package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaIngreso
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.domain.usecase.EliminarRecurrenteUseCase
import bas.orellana.gastostracker.domain.usecase.GenerarRecurrentesUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetRecurrentesUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarRecurrenteUseCase
import bas.orellana.gastostracker.domain.util.todayLocalDate
import bas.orellana.gastostracker.presentation.util.parseImporte
import bas.orellana.gastostracker.presentation.util.randomId
import bas.orellana.gastostracker.presentation.util.toPlainStringTrimmed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Recurrente que se está creando o editando. */
data class EditorRecurrente(
    val original: Recurrente? = null,
    val tipo: TipoMovimiento = TipoMovimiento.GASTO,
    val nombre: String = "",
    val monto: String = "",
    val dia: String = todayLocalDate().dayOfMonth.toString(),
    val categoria: Categoria? = null,
    val categoriaIngreso: CategoriaIngreso? = null,
    val categoriaPersonalizadaId: String? = null,
    val activo: Boolean = true
) {
    val importe: Double? get() = parseImporte(monto)
    val diaValido: Int? get() = dia.toIntOrNull()?.takeIf { it in 1..31 }
    val esValido: Boolean get() = nombre.isNotBlank() && importe != null && diaValido != null
}

data class RecurrentesState(
    val recurrentes: List<Recurrente> = emptyList(),
    val categoriasPersonalizadas: List<CategoriaPersonalizada> = emptyList(),
    val editor: EditorRecurrente? = null
) {
    val gastosFijos: List<Recurrente> get() = recurrentes.filter { it.tipo == TipoMovimiento.GASTO }
    val ingresosFijos: List<Recurrente> get() = recurrentes.filter { it.tipo == TipoMovimiento.INGRESO }

    /** Solo cuentan los activos. */
    val totalGastosFijos: Double get() = gastosFijos.sumOf { it.importeMensual }
    val totalIngresosFijos: Double get() = ingresosFijos.sumOf { it.importeMensual }
}

class RecurrentesViewModel(
    getRecurrentesUseCase: GetRecurrentesUseCase,
    getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase,
    private val guardarRecurrenteUseCase: GuardarRecurrenteUseCase,
    private val eliminarRecurrenteUseCase: EliminarRecurrenteUseCase,
    private val generarRecurrentesUseCase: GenerarRecurrentesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecurrentesState())
    val state: StateFlow<RecurrentesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getRecurrentesUseCase().catch { }.collect { lista -> _state.update { it.copy(recurrentes = lista) } }
        }
        viewModelScope.launch {
            getCategoriasPersonalizadasUseCase().catch { }
                .collect { categorias -> _state.update { it.copy(categoriasPersonalizadas = categorias) } }
        }
    }

    fun nuevo(tipo: TipoMovimiento) = _state.update { it.copy(editor = EditorRecurrente(tipo = tipo)) }

    fun editar(recurrente: Recurrente) = _state.update {
        it.copy(
            editor = EditorRecurrente(
                original = recurrente,
                tipo = recurrente.tipo,
                nombre = recurrente.nombre,
                monto = recurrente.monto.toPlainStringTrimmed().replace('.', ','),
                dia = recurrente.diaDelMes.toString(),
                categoria = recurrente.categoria,
                categoriaIngreso = recurrente.categoriaIngreso,
                categoriaPersonalizadaId = recurrente.categoriaPersonalizadaId,
                activo = recurrente.activo
            )
        )
    }

    fun actualizarEditor(cambio: (EditorRecurrente) -> EditorRecurrente) =
        _state.update { state -> state.copy(editor = state.editor?.let(cambio)) }

    fun cancelarEdicion() = _state.update { it.copy(editor = null) }

    fun guardar() {
        val editor = _state.value.editor?.takeIf { it.esValido } ?: return
        val recurrente = editor.original?.copy(
            nombre = editor.nombre.trim(),
            monto = editor.importe!!,
            diaDelMes = editor.diaValido!!,
            categoria = editor.categoria.takeIf { editor.tipo == TipoMovimiento.GASTO },
            categoriaIngreso = editor.categoriaIngreso.takeIf { editor.tipo == TipoMovimiento.INGRESO },
            categoriaPersonalizadaId = editor.categoriaPersonalizadaId,
            activo = editor.activo
        ) ?: Recurrente(
            id = randomId(),
            tipo = editor.tipo,
            nombre = editor.nombre.trim(),
            monto = editor.importe!!,
            categoria = editor.categoria.takeIf { editor.tipo == TipoMovimiento.GASTO },
            categoriaIngreso = editor.categoriaIngreso.takeIf { editor.tipo == TipoMovimiento.INGRESO },
            categoriaPersonalizadaId = editor.categoriaPersonalizadaId,
            diaDelMes = editor.diaValido!!,
            // Desde hoy: no se crean movimientos de días que ya han pasado.
            fechaInicio = todayLocalDate(),
            activo = editor.activo
        )
        _state.update { it.copy(editor = null) }
        viewModelScope.launch {
            guardarRecurrenteUseCase(recurrente)
            generarRecurrentesUseCase()
        }
    }

    fun cambiarActivo(recurrente: Recurrente, activo: Boolean) {
        viewModelScope.launch {
            guardarRecurrenteUseCase(recurrente.copy(activo = activo))
            generarRecurrentesUseCase()
        }
    }

    fun eliminarEditado() {
        val original = _state.value.editor?.original ?: return
        _state.update { it.copy(editor = null) }
        viewModelScope.launch { eliminarRecurrenteUseCase(original.id) }
    }
}
