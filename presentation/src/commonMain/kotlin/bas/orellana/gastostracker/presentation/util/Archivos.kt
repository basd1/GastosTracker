package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable

/**
 * Devuelve una función que pide al usuario dónde guardar un archivo (selector del sistema) y escribe
 * en él [contenido] como texto UTF-8. [onResultado] recibe si se ha guardado (false si se cancela).
 */
@Composable
expect fun rememberGuardarArchivo(mime: String, onResultado: (Boolean) -> Unit): (nombre: String, contenido: String) -> Unit

/**
 * Como [rememberGuardarArchivo], pero para la copia de seguridad: guarda un ZIP con [MIME_ZIP] que lleva
 * el JSON (`copia.json`) y las fotos de los tickets indicadas (`tickets/<nombre>`; las que ya no existan
 * se saltan).
 */
@Composable
expect fun rememberGuardarCopia(onResultado: (Boolean) -> Unit): (nombre: String, json: String, fotos: List<String>) -> Unit

/**
 * Devuelve una función que pide al usuario una copia de seguridad y la lee (null si se cancela o falla).
 * Acepta el ZIP de [rememberGuardarCopia] y el JSON suelto de las versiones anteriores. Las fotos del ZIP
 * se dejan aparte hasta que se confirme la restauración ([AlmacenTickets.instalarFotosRestauradas]).
 */
@Composable
expect fun rememberAbrirCopia(onLeida: (CopiaLeida?) -> Unit): () -> Unit

/** Copia de seguridad leída de un archivo: el JSON y cuántas fotos de tickets traía. */
data class CopiaLeida(val json: String, val fotos: Int = 0)

const val MIME_ZIP = "application/zip"

/** Nombre de las entradas dentro del ZIP de la copia. */
internal const val ENTRADA_JSON_COPIA = "copia.json"
internal const val CARPETA_TICKETS_COPIA = "tickets/"
