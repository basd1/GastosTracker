package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.Cuenta
import bas.orellana.gastostracker.domain.model.SaldoCuenta
import bas.orellana.gastostracker.domain.model.TipoCuenta
import bas.orellana.gastostracker.domain.usecase.EliminarCuentaUseCase
import bas.orellana.gastostracker.domain.usecase.GetSaldosCuentasUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarCuentaUseCase
import bas.orellana.gastostracker.presentation.util.randomId
import bas.orellana.gastostracker.presentation.util.toPlainStringTrimmed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorCuenta(
    val original: Cuenta? = null,
    val nombre: String = "",
    val tipo: TipoCuenta = TipoCuenta.BANCO,
    val saldoInicial: String = "",
    val color: Long = Categoria.PALETA_COLORES_PERSONALIZADOS[5],
    val archivada: Boolean = false
) {
    /** Vacío = 0; admite coma decimal y saldo negativo (p. ej. una tarjeta de crédito que ya se debe). */
    val importeInicial: Double? get() = if (saldoInicial.isBlank()) 0.0 else saldoInicial.trim().replace(',', '.').toDoubleOrNull()
    val esValido: Boolean get() = nombre.isNotBlank() && importeInicial != null
}

data class CuentasState(
    val saldos: List<SaldoCuenta> = emptyList(),
    val editor: EditorCuenta? = null,
    val cuentaAEliminar: Cuenta? = null
) {
    /** Cuentas que se pueden elegir en un gasto o ingreso nuevo. */
    val activas: List<Cuenta> get() = saldos.map { it.cuenta }.filterNot { it.archivada }
    val saldoTotal: Double get() = saldos.filterNot { it.cuenta.archivada }.sumOf { it.saldo }
}

class CuentasViewModel(
    getSaldosCuentasUseCase: GetSaldosCuentasUseCase,
    private val guardarCuentaUseCase: GuardarCuentaUseCase,
    private val eliminarCuentaUseCase: EliminarCuentaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CuentasState())
    val state: StateFlow<CuentasState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getSaldosCuentasUseCase().catch { }.collect { saldos -> _state.update { it.copy(saldos = saldos) } }
        }
    }

    fun nueva() = _state.update { it.copy(editor = EditorCuenta()) }

    fun editar(cuenta: Cuenta) = _state.update {
        it.copy(
            editor = EditorCuenta(
                original = cuenta,
                nombre = cuenta.nombre,
                tipo = cuenta.tipo,
                saldoInicial = cuenta.saldoInicial.toPlainStringTrimmed().replace('.', ','),
                color = cuenta.color,
                archivada = cuenta.archivada
            )
        )
    }

    fun actualizarEditor(cambio: (EditorCuenta) -> EditorCuenta) =
        _state.update { state -> state.copy(editor = state.editor?.let(cambio)) }

    fun cancelarEdicion() = _state.update { it.copy(editor = null) }

    fun guardar() {
        val editor = _state.value.editor?.takeIf { it.esValido } ?: return
        val cuenta = Cuenta(
            id = editor.original?.id ?: randomId(),
            nombre = editor.nombre.trim(),
            tipo = editor.tipo,
            saldoInicial = editor.importeInicial ?: 0.0,
            color = editor.color,
            archivada = editor.archivada
        )
        _state.update { it.copy(editor = null) }
        viewModelScope.launch { guardarCuentaUseCase(cuenta) }
    }

    fun pedirEliminar() {
        val cuenta = _state.value.editor?.original ?: return
        _state.update { it.copy(editor = null, cuentaAEliminar = cuenta) }
    }

    fun cancelarEliminar() = _state.update { it.copy(cuentaAEliminar = null) }

    fun confirmarEliminar() {
        val cuenta = _state.value.cuentaAEliminar ?: return
        _state.update { it.copy(cuentaAEliminar = null) }
        viewModelScope.launch { eliminarCuentaUseCase(cuenta.id) }
    }
}
