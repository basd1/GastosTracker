package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable

/**
 * Devuelve una función que pide al usuario dónde guardar un archivo (selector del sistema) y escribe
 * en él [contenido] como texto UTF-8. [onResultado] recibe si se ha guardado (false si se cancela).
 */
@Composable
expect fun rememberGuardarArchivo(mime: String, onResultado: (Boolean) -> Unit): (nombre: String, contenido: String) -> Unit

/** Devuelve una función que pide al usuario un archivo y lo lee como texto (null si se cancela o falla). */
@Composable
expect fun rememberAbrirArchivo(onLeido: (String?) -> Unit): () -> Unit
