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
