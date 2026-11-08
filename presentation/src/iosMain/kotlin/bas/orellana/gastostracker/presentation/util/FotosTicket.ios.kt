package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO(iOS): UIImagePickerController / PHPickerViewController para la foto y Vision (VNRecognizeTextRequest)
// para leer el texto. Mientras tanto, en iOS no se ofrece adjuntar el ticket.

@Composable
actual fun rememberSelectorFotoTicket(onFoto: (String?) -> Unit): SelectorFotoTicket =
    SelectorFotoTicket(disponible = false, camara = { onFoto(null) }, galeria = { onFoto(null) })

@Composable
actual fun rememberLectorTicket(): suspend (ruta: String) -> String? = { null }

@Composable
actual fun ImagenTicket(ruta: String, modifier: Modifier, contentDescription: String?) = Unit
