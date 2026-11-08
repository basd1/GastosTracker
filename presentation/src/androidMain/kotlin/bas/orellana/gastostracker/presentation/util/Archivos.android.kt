package bas.orellana.gastostracker.presentation.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

/** Storage Access Framework: el usuario elige dónde guardar; la app no necesita permisos de almacenamiento. */
@Composable
actual fun rememberGuardarArchivo(mime: String, onResultado: (Boolean) -> Unit): (nombre: String, contenido: String) -> Unit {
    val context = LocalContext.current
    val resultado = rememberUpdatedState(onResultado)
    val pendiente = remember { arrayOfNulls<String>(1) }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mime)) { uri ->
        val contenido = pendiente[0]
        pendiente[0] = null
        if (uri == null || contenido == null) {
            resultado.value(false)
            return@rememberLauncherForActivityResult
        }
        val guardado = runCatching {
            context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(contenido.encodeToByteArray()) } != null
        }.getOrDefault(false)
        resultado.value(guardado)
    }
    return remember(lanzador) {
        { nombre, contenido ->
            pendiente[0] = contenido
            lanzador.launch(nombre)
        }
    }
}

@Composable
actual fun rememberAbrirArchivo(onLeido: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val leido = rememberUpdatedState(onLeido)
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            leido.value(null)
            return@rememberLauncherForActivityResult
        }
        val texto = runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
        }.getOrNull()
        leido.value(texto)
    }
    return remember(lanzador) {
        { lanzador.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
    }
}
