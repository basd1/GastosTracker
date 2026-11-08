package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.MetaAhorro
import bas.orellana.gastostracker.domain.model.ProgresoMeta
import bas.orellana.gastostracker.domain.usecase.GetProgresoMetasUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalAhorradoUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarAportacionUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarMetaUseCase
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import bas.orellana.gastostracker.presentation.util.diaInicioMes
import kotlinx.coroutines.flow.flatMapLatest
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
import kotlinx.datetime.LocalDate

/** Meta que se está creando o editando. */
data class EditorMeta(
    val original: MetaAhorro? = null,
    val nombre: String = "",
    val objetivo: String = "",
    val fechaObjetivo: LocalDate? = null,
    val color: Long = Categoria.PALETA_COLORES_PERSONALIZADOS.first()
) {
    /** null si el objetivo está vacío (es opcional). */
    val importeObjetivo: Double? get() = parseImporte(objetivo)
    val objetivoInvalido: Boolean get() = objetivo.isNotBlank() && importeObjetivo == null
    val esValido: Boolean get() = nombre.isNotBlank() && !objetivoInvalido

    fun aMeta(): MetaAhorro = (original ?: MetaAhorro(id = randomId(), nombre = "", color = color, creada = todayLocalDate()))
        .copy(nombre = nombre.trim(), objetivo = importeObjetivo, fechaObjetivo = fechaObjetivo, color = color)

    companion object {
        fun de(meta: MetaAhorro) = EditorMeta(
            original = meta,
            nombre = meta.nombre,
            objetivo = meta.objetivo?.toPlainStringTrimmed()?.replace('.', ',').orEmpty(),
            fechaObjetivo = meta.fechaObjetivo,
            color = meta.color
        )
    }
}

/** Aportación (o retirada) que se está apuntando en una meta. */
data class EditorAportacion(
    val meta: MetaAhorro,
    val esRetirada: Boolean = false,
    val monto: String = "",
    val fecha: LocalDate = todayLocalDate(),
    val nota: String = ""
) {
    val importe: Double? get() = parseImporte(monto)

    fun aAportacion(): Aportacion? {
        val importe = importe ?: return null
        return Aportacion(
            id = randomId(),
            metaId = meta.id,
            monto = if (esRetirada) -importe else importe,
            fecha = fecha,
            nota = nota.trim().ifEmpty { null }
        )
    }
}

data class MetasState(
    val metas: List<ProgresoMeta> = emptyList(),
    val ahorradoEsteMes: Double = 0.0,
    val editorMeta: EditorMeta? = null,
    val editorAportacion: EditorAportacion? = null
) {
    val totalAhorrado: Double get() = metas.sumOf { it.ahorrado }
    val activas: List<ProgresoMeta> get() = metas.filterNot { it.meta.archivada }
    val archivadas: List<ProgresoMeta> get() = metas.filter { it.meta.archivada }
}

class MetasViewModel(
    getProgresoMetasUseCase: GetProgresoMetasUseCase,
    getTotalAhorradoUseCase: GetTotalAhorradoUseCase,
    private val guardarMetaUseCase: GuardarMetaUseCase,
    private val guardarAportacionUseCase: GuardarAportacionUseCase,
    ajustesRepository: AjustesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MetasState())
    val state: StateFlow<MetasState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getProgresoMetasUseCase().catch { }.collect { metas -> _state.update { it.copy(metas = metas) } }
        }
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            ajustesRepository.diaInicioMes(this)
                .flatMapLatest { dia -> Mes.actual(dia).periodo(dia).let { getTotalAhorradoUseCase(it.start, it.endInclusive) } }
                .catch { }
                .collect { total -> _state.update { it.copy(ahorradoEsteMes = total) } }
        }
    }

    fun nuevaMeta() {
        // Un color de la paleta que no use ya otra meta, si queda alguno.
        val usados = _state.value.metas.map { it.meta.color }.toSet()
        val color = Categoria.PALETA_COLORES_PERSONALIZADOS.firstOrNull { it !in usados }
            ?: Categoria.PALETA_COLORES_PERSONALIZADOS.first()
        _state.update { it.copy(editorMeta = EditorMeta(color = color)) }
    }

    fun actualizarEditorMeta(cambio: (EditorMeta) -> EditorMeta) =
        _state.update { state -> state.copy(editorMeta = state.editorMeta?.let(cambio)) }

    fun cancelarEditorMeta() = _state.update { it.copy(editorMeta = null) }

    fun guardarMeta() {
        val editor = _state.value.editorMeta?.takeIf { it.esValido } ?: return
        _state.update { it.copy(editorMeta = null) }
        viewModelScope.launch { guardarMetaUseCase(editor.aMeta()) }
    }

    fun aportar(meta: MetaAhorro, retirada: Boolean = false) =
        _state.update { it.copy(editorAportacion = EditorAportacion(meta, esRetirada = retirada)) }

    fun actualizarEditorAportacion(cambio: (EditorAportacion) -> EditorAportacion) =
        _state.update { state -> state.copy(editorAportacion = state.editorAportacion?.let(cambio)) }

    fun cancelarAportacion() = _state.update { it.copy(editorAportacion = null) }

    fun guardarAportacion() {
        val aportacion = _state.value.editorAportacion?.aAportacion() ?: return
        _state.update { it.copy(editorAportacion = null) }
        viewModelScope.launch { guardarAportacionUseCase(aportacion) }
    }
}
