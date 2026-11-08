package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /** null mientras no se ha leído el ajuste guardado. Si no se puede leer, tema claro (y no bloquea el splash). */
    private val temaGuardado: StateFlow<Boolean?> = preferencesRepository.isDarkTheme
        .catch { emit(false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isDarkTheme: StateFlow<Boolean> = temaGuardado
        .map { it ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** true en cuanto se han leído las preferencias guardadas (sirve para mantener el splash hasta entonces). */
    val preferenciasCargadas: StateFlow<Boolean> = temaGuardado
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleDarkTheme() {
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(!isDarkTheme.value)
        }
    }
}
