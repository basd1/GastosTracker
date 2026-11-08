package bas.orellana.gastostracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import bas.orellana.gastostracker.data.preferences.ClavesPreferencias
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Recibe el DataStore de preferencias (solo ajustes); los datos viven en Room. */
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val isDarkTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ClavesPreferencias.DARK_THEME] ?: false
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[ClavesPreferencias.DARK_THEME] = isDark
        }
    }
}
