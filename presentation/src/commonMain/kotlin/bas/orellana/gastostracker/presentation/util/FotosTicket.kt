package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Formas de conseguir la foto de un ticket. [disponible] es false en plataformas sin implementar. */
class SelectorFotoTicket(
    val disponible: Boolean,
    val camara: () -> Unit,
    val galeria: () -> Unit
)

/**
 * Cámara o galería. La foto se copia al almacenamiento privado de la app y [onFoto] recibe su ruta
 * (o null si se cancela o falla).
 */
@Composable
expect fun rememberSelectorFotoTicket(onFoto: (String?) -> Unit): SelectorFotoTicket

/** Devuelve una función que reconoce el texto de la foto de un ticket (null si no se puede). */
@Composable
expect fun rememberLectorTicket(): suspend (ruta: String) -> String?

/** Muestra la foto guardada en [ruta] (o nada si ya no existe). */
@Composable
expect fun ImagenTicket(ruta: String, modifier: Modifier = Modifier, contentDescription: String? = null)

/**
 * Nombre del fichero de la foto. Las fotos se buscan por nombre dentro de la carpeta de tickets, así la
 * ruta guardada en un gasto sigue sirviendo aunque venga de otro móvil (una copia restaurada).
 */
fun nombreFotoTicket(ruta: String): String = ruta.substringAfterLast('/').substringAfterLast('\\')

/** Una foto de la carpeta de tickets y cuándo se modificó por última vez (milisegundos desde 1970). */
data class FotoGuardada(val nombre: String, val modificadaMs: Long)

/** Las fotos de tickets guardadas en el almacenamiento privado de la app. */
interface AlmacenTickets {
    suspend fun fotos(): List<FotoGuardada>

    suspend fun borrar(nombres: Collection<String>)

    /** Pasa a la carpeta de tickets las fotos de la última copia abierta con [rememberAbrirCopia]. */
    suspend fun instalarFotosRestauradas()
}
