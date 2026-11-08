package bas.orellana.gastostracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bas.orellana.gastostracker.data.legacy.FuentesLegacy
import bas.orellana.gastostracker.data.legacy.LEGACY_FILE_NAME
import bas.orellana.gastostracker.data.preferences.PREFERENCIAS_FILE_NAME
import bas.orellana.gastostracker.data.preferences.abrirPreferenciasDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun crearPreferenciasDataStore(): DataStore<Preferences> =
    abrirPreferenciasDataStore(
        ruta = "${directorioDocumentos()}/$PREFERENCIAS_FILE_NAME".toPath(),
        fuentesLegacy = fuentesLegacy()
    )

/** En iOS solo existió la versión KMP, que guardaba el JSON en Documents/settings.preferences_pb. */
fun fuentesLegacy(): FuentesLegacy = FuentesLegacy(
    rutas = listOf("${directorioDocumentos()}/$LEGACY_FILE_NAME".toPath()),
    fileSystem = FileSystem.SYSTEM
)

@OptIn(ExperimentalForeignApi::class)
internal fun directorioDocumentos(): String {
    val documentos = requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    )
    return requireNotNull(documentos.path)
}
