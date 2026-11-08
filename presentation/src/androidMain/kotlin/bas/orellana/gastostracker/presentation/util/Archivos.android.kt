package bas.orellana.gastostracker.presentation.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Escribe el contenido de un archivo en la salida elegida por el usuario. */
private typealias Escritor = (Context, OutputStream) -> Unit

/** Storage Access Framework: el usuario elige dónde guardar; la app no necesita permisos de almacenamiento. */
@Composable
actual fun rememberGuardarArchivo(mime: String, onResultado: (Boolean) -> Unit): (nombre: String, contenido: String) -> Unit {
    val guardar = rememberGuardarConEscritor(mime, onResultado)
    return remember(guardar) {
        { nombre, contenido -> guardar(nombre) { _, salida -> salida.write(contenido.encodeToByteArray()) } }
    }
}

@Composable
actual fun rememberGuardarCopia(onResultado: (Boolean) -> Unit): (nombre: String, json: String, fotos: List<String>) -> Unit {
    val guardar = rememberGuardarConEscritor(MIME_ZIP, onResultado)
    return remember(guardar) {
        { nombre, json, fotos -> guardar(nombre) { context, salida -> escribirCopiaZip(context, salida, json, fotos) } }
    }
}

private fun escribirCopiaZip(context: Context, salida: OutputStream, json: String, fotos: List<String>) {
    ZipOutputStream(salida).use { zip ->
        zip.putNextEntry(ZipEntry(ENTRADA_JSON_COPIA))
        zip.write(json.encodeToByteArray())
        zip.closeEntry()
        fotos.map(::nombreFotoTicket).distinct().forEach { nombreFoto ->
            val foto = archivoTicket(context, nombreFoto)
            if (!foto.isFile) return@forEach
            zip.putNextEntry(ZipEntry(CARPETA_TICKETS_COPIA + nombreFoto))
            foto.inputStream().use { it.copyTo(zip) }
            zip.closeEntry()
        }
    }
}

/**
 * Abre el selector de "Guardar como" y escribe en el archivo elegido fuera del hilo principal
 * (una copia con fotos puede tardar).
 */
@Composable
private fun rememberGuardarConEscritor(mime: String, onResultado: (Boolean) -> Unit): (nombre: String, escribir: Escritor) -> Unit {
    val context = LocalContext.current
    val resultado = rememberUpdatedState(onResultado)
    val scope = rememberCoroutineScope()
    val pendiente = remember { arrayOfNulls<Escritor>(1) }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mime)) { uri ->
        val escribir = pendiente[0]
        pendiente[0] = null
        if (uri == null || escribir == null) {
            resultado.value(false)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val guardado = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { escribir(context, it) } != null
                }.getOrDefault(false)
            }
            resultado.value(guardado)
        }
    }
    return remember(lanzador) {
        { nombre, escribir ->
            pendiente[0] = escribir
            lanzador.launch(nombre)
        }
    }
}

@Composable
actual fun rememberAbrirCopia(onLeida: (CopiaLeida?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val leida = rememberUpdatedState(onLeida)
    val scope = rememberCoroutineScope()
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            leida.value(null)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val copia = withContext(Dispatchers.IO) { runCatching { leerCopia(context, uri) }.getOrNull() }
            leida.value(copia)
        }
    }
    return remember(lanzador) {
        { lanzador.launch(arrayOf(MIME_ZIP, "application/json", "text/plain", "application/octet-stream")) }
    }
}

/** Lee una copia ZIP (JSON + fotos, que se dejan en [carpetaRestaurar]) o un JSON suelto de versiones anteriores. */
private fun leerCopia(context: Context, uri: Uri): CopiaLeida? {
    val restaurar = carpetaRestaurar(context)
    restaurar.deleteRecursively()
    val entrada = context.contentResolver.openInputStream(uri) ?: return null
    return BufferedInputStream(entrada).use { stream ->
        stream.mark(2)
        val esZip = stream.read() == 'P'.code && stream.read() == 'K'.code
        stream.reset()
        if (esZip) leerZip(ZipInputStream(stream), restaurar) else CopiaLeida(stream.readBytes().decodeToString())
    }
}

private fun leerZip(zip: ZipInputStream, restaurar: File): CopiaLeida? {
    var json: String? = null
    var fotos = 0
    while (true) {
        val entrada = zip.nextEntry ?: break
        val nombre = entrada.name
        when {
            entrada.isDirectory -> Unit
            nombre == ENTRADA_JSON_COPIA -> json = zip.readBytes().decodeToString()
            nombre.startsWith(CARPETA_TICKETS_COPIA) -> {
                // Solo el nombre: una entrada con "../" no puede salirse de la carpeta.
                val nombreFoto = nombreFotoTicket(nombre)
                if (nombreFoto.isNotBlank() && nombreFoto != "." && nombreFoto != "..") {
                    restaurar.mkdirs()
                    File(restaurar, nombreFoto).outputStream().use { zip.copyTo(it) }
                    fotos++
                }
            }
        }
        zip.closeEntry()
    }
    return json?.let { CopiaLeida(it, fotos) }
}
