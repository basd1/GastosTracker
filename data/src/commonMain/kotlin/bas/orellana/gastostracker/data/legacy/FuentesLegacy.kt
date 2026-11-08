package bas.orellana.gastostracker.data.legacy

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

/** Nombre del fichero de DataStore que usaban las versiones anteriores (datos en JSON + tema). */
internal const val LEGACY_FILE_NAME = "settings.preferences_pb"

internal object ClavesLegacy {
    val GASTOS = stringPreferencesKey("gastos")
    val INGRESOS = stringPreferencesKey("ingresos")
    val CATEGORIAS_PERSONALIZADAS = stringPreferencesKey("categorias_personalizadas")
    val DARK_THEME = booleanPreferencesKey("dark_theme")
}

/**
 * Ficheros de DataStore de versiones anteriores, ordenados del más antiguo al más reciente.
 *
 * Se leen directamente con [PreferencesSerializer], sin abrir un DataStore sobre ellos,
 * así que nunca se modifican: quedan como copia de seguridad.
 */
class FuentesLegacy internal constructor(
    private val rutas: List<Path>,
    private val fileSystem: FileSystem
) {
    /** Devuelve el contenido de los ficheros que existan. Lanza si alguno existe pero no se puede leer. */
    internal suspend fun leer(): List<Preferences> =
        rutas.filter { fileSystem.exists(it) }.map { ruta ->
            fileSystem.source(ruta).buffer().use { PreferencesSerializer.readFrom(it) }
        }
}
