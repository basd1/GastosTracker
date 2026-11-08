package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.ProgresoMeta
import bas.orellana.gastostracker.domain.usecase.EliminarAportacionUseCase
import bas.orellana.gastostracker.domain.usecase.EliminarMetaUseCase
import bas.orellana.gastostracker.domain.usecase.GetAportacionesUseCase
import bas.orellana.gastostracker.domain.usecase.GetProgresoMetasUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarAportacionUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarMetaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MetaDetalleState(
    val progreso: ProgresoMeta? = null,
    val aportaciones: List<Aportacion> = emptyList(),
    val editorMeta: EditorMeta? = null,
    val editorAportacion: EditorAportacion? = null,
    val confirmarEliminar: Boolean = false,
    /** La meta se ha eliminado: la pantalla debe cerrarse. */
    val eliminada: Boolean = false
)

class MetaDetalleViewModel(
    private val metaId: String,
    getProgresoMetasUseCase: GetProgresoMetasUseCase,
    getAportacionesUseCase: GetAportacionesUseCase,
    private val guardarMetaUseCase: GuardarMetaUseCase,
    private val eliminarMetaUseCase: EliminarMetaUseCase,
    private val guardarAportacionUseCase: GuardarAportacionUseCase,
    private val eliminarAportacionUseCase: EliminarAportacionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MetaDetalleState())
    val state: StateFlow<MetaDetalleState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getProgresoMetasUseCase().catch { }
                .collect { metas -> _state.update { it.copy(progreso = metas.find { m -> m.meta.id == metaId }) } }
        }
        viewModelScope.launch {
            getAportacionesUseCase(metaId).catch { }
                .collect { lista -> _state.update { it.copy(aportaciones = lista) } }
        }
    }

    fun editarMeta() {
        val meta = _state.value.progreso?.meta ?: return
        _state.update { it.copy(editorMeta = EditorMeta.de(meta)) }
    }

    fun actualizarEditorMeta(cambio: (EditorMeta) -> EditorMeta) =
        _state.update { state -> state.copy(editorMeta = state.editorMeta?.let(cambio)) }

    fun cancelarEditorMeta() = _state.update { it.copy(editorMeta = null) }

    fun guardarMeta() {
        val editor = _state.value.editorMeta?.takeIf { it.esValido } ?: return
        _state.update { it.copy(editorMeta = null) }
        viewModelScope.launch { guardarMetaUseCase(editor.aMeta()) }
    }

    fun cambiarArchivada() {
        val meta = _state.value.progreso?.meta ?: return
        viewModelScope.launch { guardarMetaUseCase(meta.copy(archivada = !meta.archivada)) }
    }

    fun pedirEliminar() = _state.update { it.copy(confirmarEliminar = true) }

    fun cancelarEliminar() = _state.update { it.copy(confirmarEliminar = false) }

    fun eliminarMeta() {
        _state.update { it.copy(confirmarEliminar = false) }
        viewModelScope.launch {
            eliminarMetaUseCase(metaId)
            _state.update { it.copy(eliminada = true) }
        }
    }

    fun aportar(retirada: Boolean) {
        val meta = _state.value.progreso?.meta ?: return
        _state.update { it.copy(editorAportacion = EditorAportacion(meta, esRetirada = retirada)) }
    }

    fun actualizarEditorAportacion(cambio: (EditorAportacion) -> EditorAportacion) =
        _state.update { state -> state.copy(editorAportacion = state.editorAportacion?.let(cambio)) }

    fun cancelarAportacion() = _state.update { it.copy(editorAportacion = null) }

    fun guardarAportacion() {
        val aportacion = _state.value.editorAportacion?.aAportacion() ?: return
        _state.update { it.copy(editorAportacion = null) }
        viewModelScope.launch { guardarAportacionUseCase(aportacion) }
    }

    fun eliminarAportacion(aportacion: Aportacion) {
        viewModelScope.launch { eliminarAportacionUseCase(aportacion.id) }
    }

    fun restaurarAportacion(aportacion: Aportacion) {
        viewModelScope.launch { guardarAportacionUseCase(aportacion) }
    }
}
