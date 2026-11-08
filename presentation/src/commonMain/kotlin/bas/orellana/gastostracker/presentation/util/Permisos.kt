package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable

/**
 * Devuelve una función que pide permiso para mostrar notificaciones (si hace falta) y llama a
 * [onResultado] con si está concedido.
 */
@Composable
expect fun rememberPedirPermisoNotificaciones(onResultado: (Boolean) -> Unit): () -> Unit
