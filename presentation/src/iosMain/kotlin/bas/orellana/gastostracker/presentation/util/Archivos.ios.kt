package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable

// TODO(iOS): usar UIDocumentPickerViewController para guardar y abrir archivos. Mientras tanto, la
// exportación y la restauración no están disponibles en iOS (la app avisa de que no se ha podido).

@Composable
actual fun rememberGuardarArchivo(mime: String, onResultado: (Boolean) -> Unit): (nombre: String, contenido: String) -> Unit =
    { _, _ -> onResultado(false) }

@Composable
actual fun rememberGuardarCopia(onResultado: (Boolean) -> Unit): (nombre: String, json: String, fotos: List<String>) -> Unit =
    { _, _, _ -> onResultado(false) }

@Composable
actual fun rememberAbrirCopia(onLeida: (CopiaLeida?) -> Unit): () -> Unit = { onLeida(null) }
