package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable

// TODO(iOS): usar UIDocumentPickerViewController para guardar y abrir archivos. Mientras tanto, la
// exportación y la restauración no están disponibles en iOS (la app avisa de que no se ha podido).

@Composable
actual fun rememberGuardarArchivo(mime: String, onResultado: (Boolean) -> Unit): (nombre: String, contenido: String) -> Unit =
    { _, _ -> onResultado(false) }

@Composable
actual fun rememberAbrirArchivo(onLeido: (String?) -> Unit): () -> Unit = { onLeido(null) }
