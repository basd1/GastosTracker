package bas.orellana.gastostracker.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/** Carpeta privada donde se guardan las fotos de los tickets (entra en la copia de seguridad de Android). */
internal fun carpetaTickets(context: Context): File = File(context.filesDir, "tickets").apply { mkdirs() }

/** Fichero de la foto guardada en [ruta], buscado por nombre en la carpeta de tickets (ver [nombreFotoTicket]). */
internal fun archivoTicket(context: Context, ruta: String): File = File(carpetaTickets(context), nombreFotoTicket(ruta))

/** Donde se dejan las fotos de una copia abierta hasta que se confirma la restauración. */
internal fun carpetaRestaurar(context: Context): File = File(context.cacheDir, "restaurar-tickets")

class AlmacenTicketsAndroid(private val context: Context) : AlmacenTickets {

    override suspend fun fotos(): List<FotoGuardada> = withContext(Dispatchers.IO) {
        carpetaTickets(context).listFiles().orEmpty()
            .filter { it.isFile }
            .map { FotoGuardada(it.name, it.lastModified()) }
    }

    override suspend fun borrar(nombres: Collection<String>) = withContext(Dispatchers.IO) {
        nombres.forEach { archivoTicket(context, it).delete() }
    }

    override suspend fun instalarFotosRestauradas() = withContext(Dispatchers.IO) {
        val origen = carpetaRestaurar(context)
        origen.listFiles().orEmpty().filter { it.isFile }.forEach { foto ->
            val destino = File(carpetaTickets(context), foto.name)
            destino.delete()
            if (!foto.renameTo(destino)) {
                foto.copyTo(destino, overwrite = true)
                foto.delete()
            }
            // Como recién guardada, para que la limpieza de fotos sin gasto no la toque hoy.
            destino.setLastModified(System.currentTimeMillis())
        }
        origen.deleteRecursively()
        Unit
    }
}

/** Copia la imagen de [uri] a la carpeta de tickets y devuelve la ruta (null si falla). */
private fun copiarATickets(context: Context, uri: Uri): String? = runCatching {
    val destino = File(carpetaTickets(context), "${randomId()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { entrada -> destino.outputStream().use { entrada.copyTo(it) } }
        ?: return null
    destino.absolutePath
}.getOrNull()

@Composable
actual fun rememberSelectorFotoTicket(onFoto: (String?) -> Unit): SelectorFotoTicket {
    val context = LocalContext.current
    val alTerminar = rememberUpdatedState(onFoto)
    // La cámara escribe en un fichero temporal compartido con FileProvider (ver res/xml/rutas_archivos.xml de :app).
    val temporal = remember { File(File(context.cacheDir, "fotos").apply { mkdirs() }, "ticket.jpg") }
    val uriTemporal = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.archivos", temporal)
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { hecha ->
        alTerminar.value(if (hecha) copiarATickets(context, uriTemporal).also { temporal.delete() } else null)
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        alTerminar.value(uri?.let { copiarATickets(context, it) })
    }
    return remember(camara, galeria) {
        SelectorFotoTicket(
            disponible = true,
            camara = { camara.launch(uriTemporal) },
            galeria = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        )
    }
}

@Composable
actual fun rememberLectorTicket(): suspend (ruta: String) -> String? {
    val context = LocalContext.current
    return remember(context) {
        { ruta ->
            runCatching {
                // fromFilePath respeta la orientación EXIF de la foto.
                val imagen = InputImage.fromFilePath(context, Uri.fromFile(archivoTicket(context, ruta)))
                val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                try {
                    suspendCancellableCoroutine { continuacion ->
                        reconocedor.process(imagen)
                            .addOnSuccessListener { continuacion.resume(it.text) }
                            .addOnFailureListener { continuacion.resume(null) }
                    }
                } finally {
                    reconocedor.close()
                }
            }.getOrNull()
        }
    }
}

@Composable
actual fun ImagenTicket(ruta: String, modifier: Modifier, contentDescription: String?) {
    val context = LocalContext.current
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) { cargarReducida(archivoTicket(context, ruta).path, ladoMaximo = 1600)?.asImageBitmap() }
    }
    imagen?.let { Image(bitmap = it, contentDescription = contentDescription, modifier = modifier, contentScale = ContentScale.Fit) }
}

/** Carga la foto reducida (para no gastar memoria) y girada según su EXIF. */
private fun cargarReducida(ruta: String, ladoMaximo: Int): Bitmap? = runCatching {
    val fichero = File(ruta)
    if (!fichero.exists()) return null
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, limites)
    var muestreo = 1
    while (maxOf(limites.outWidth, limites.outHeight) / muestreo > ladoMaximo) muestreo *= 2
    val bitmap = BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = muestreo }) ?: return null
    val grados = when (ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (grados == 0f) bitmap
    else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true)
}.getOrNull()
