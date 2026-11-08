package bas.orellana.gastostracker.presentation.util

import kotlinx.coroutines.CancellationException

/**
 * Ejecuta una tarea secundaria sin que un fallo tumbe la app (p. ej. generar recurrentes al abrir).
 * Las cancelaciones se propagan; el resto de errores se ignoran y se reintentará la próxima vez.
 */
suspend fun ejecutarSinFallar(@Suppress("UNUSED_PARAMETER") descripcion: String, bloque: suspend () -> Unit) {
    try {
        bloque()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        // Tarea secundaria: no hay nada que mostrar al usuario.
    }
}
