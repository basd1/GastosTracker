package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable

// TODO(iOS): pedir permiso con UNUserNotificationCenter y programar el aviso con UNCalendarNotificationTrigger.
// De momento el recordatorio diario solo funciona en Android.
@Composable
actual fun rememberPedirPermisoNotificaciones(onResultado: (Boolean) -> Unit): () -> Unit = { onResultado(true) }
