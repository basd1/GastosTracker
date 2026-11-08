package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Ajustes
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val ajustesRepository: AjustesRepository
) : ViewModel() {

    /** null mientras no se han leído los ajustes guardados. Si no se pueden leer, los de por defecto. */
    private val guardados: StateFlow<Ajustes?> = ajustesRepository.ajustes
        .catch { emit(Ajustes()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val ajustes: StateFlow<Ajustes> = guardados
        .map { it ?: Ajustes() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Ajustes())

    val isDarkTheme: StateFlow<Boolean> = ajustes
        .map { it.temaOscuro }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** true en cuanto se han leído las preferencias guardadas (sirve para mantener el splash hasta entonces). */
    val preferenciasCargadas: StateFlow<Boolean> = guardados
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleDarkTheme() {
        viewModelScope.launch { ajustesRepository.setTemaOscuro(!isDarkTheme.value) }
    }

    fun setMoneda(codigo: String) {
        viewModelScope.launch { ajustesRepository.setMoneda(codigo) }
    }

    fun setDiaInicioMes(dia: Int) {
        viewModelScope.launch { ajustesRepository.setDiaInicioMes(dia) }
    }

    fun setRecordatorio(activo: Boolean, hora: Int = ajustes.value.recordatorioHora, minuto: Int = ajustes.value.recordatorioMinuto) {
        viewModelScope.launch { ajustesRepository.setRecordatorio(activo, hora, minuto) }
    }

    fun setBloqueo(activo: Boolean) {
        viewModelScope.launch { ajustesRepository.setBloqueo(activo) }
    }
}
