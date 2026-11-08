package bas.orellana.gastostracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bas.orellana.gastostracker.data.legacy.FuentesLegacy
import bas.orellana.gastostracker.data.legacy.LEGACY_FILE_NAME
import bas.orellana.gastostracker.data.preferences.PREFERENCIAS_FILE_NAME
import bas.orellana.gastostracker.data.preferences.abrirPreferenciasDataStore
import okio.FileSystem
import okio.Path.Companion.toPath

fun crearPreferenciasDataStore(context: Context): DataStore<Preferences> =
    abrirPreferenciasDataStore(
        ruta = context.filesDir.resolve(PREFERENCIAS_FILE_NAME).absolutePath.toPath(),
        fuentesLegacy = fuentesLegacy(context)
    )

/** Ficheros antiguos en Android, del más antiguo al más reciente. */
fun fuentesLegacy(context: Context): FuentesLegacy = FuentesLegacy(
    rutas = listOf(
        // App Android original: `preferencesDataStore(name = "settings")` → files/datastore/settings.preferences_pb
        context.filesDir.resolve("datastore/$LEGACY_FILE_NAME").absolutePath.toPath(),
        // Primera versión KMP (JSON en DataStore creado con createWithPath) → files/settings.preferences_pb
        context.filesDir.resolve(LEGACY_FILE_NAME).absolutePath.toPath(),
    ),
    fileSystem = FileSystem.SYSTEM
)
