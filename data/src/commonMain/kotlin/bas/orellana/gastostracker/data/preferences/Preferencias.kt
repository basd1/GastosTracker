package bas.orellana.gastostracker.data.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import bas.orellana.gastostracker.data.legacy.ClavesLegacy
import bas.orellana.gastostracker.data.legacy.FuentesLegacy
import bas.orellana.gastostracker.data.util.registrarError
import kotlinx.coroutines.CancellationException
import okio.Path

/** Fichero de preferencias de la app (solo ajustes, sin datos). */
// Si cambia el nombre o la ruta, revisa app/src/main/res/xml/data_extraction_rules.xml (copia de seguridad).
internal const val PREFERENCIAS_FILE_NAME = "preferencias.preferences_pb"

internal object ClavesPreferencias {
    val DARK_THEME = booleanPreferencesKey("dark_theme")

    /** El tema ya se copió desde los ficheros antiguos. */
    val PREFERENCIAS_MIGRADAS = booleanPreferencesKey("preferencias_migradas")

    /** Gastos, ingresos y categorías ya se copiaron desde el JSON antiguo a Room. */
    val DATOS_MIGRADOS_A_ROOM = booleanPreferencesKey("datos_migrados_a_room")
}

internal fun abrirPreferenciasDataStore(ruta: Path, fuentesLegacy: FuentesLegacy): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        migrations = listOf(MigracionPreferenciasLegacy(fuentesLegacy)),
        produceFile = { ruta }
    )

/** Copia el ajuste del tema oscuro de los ficheros antiguos la primera vez que se abre. */
private class MigracionPreferenciasLegacy(private val fuentes: FuentesLegacy) : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData[ClavesPreferencias.PREFERENCIAS_MIGRADAS] != true

    override suspend fun migrate(currentData: Preferences): Preferences {
        val temaOscuro = try {
            // Las fuentes van de la más antigua a la más reciente: gana el último valor guardado.
            fuentes.leer().mapNotNull { it[ClavesLegacy.DARK_THEME] }.lastOrNull()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Perder el ajuste del tema no es grave: no bloqueamos la app por ello.
            registrarError(TAG, "No se pudo leer el tema de los ficheros antiguos", e)
            null
        }
        return currentData.toMutablePreferences().apply {
            temaOscuro?.let { this[ClavesPreferencias.DARK_THEME] = it }
            this[ClavesPreferencias.PREFERENCIAS_MIGRADAS] = true
        }
    }

    override suspend fun cleanUp() = Unit

    private companion object {
        const val TAG = "MigracionPreferencias"
    }
}
