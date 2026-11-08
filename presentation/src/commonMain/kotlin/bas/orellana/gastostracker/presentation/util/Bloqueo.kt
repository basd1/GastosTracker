package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable

/** true si el dispositivo tiene huella, cara, PIN, patrón o contraseña con los que desbloquear la app. */
@Composable
expect fun bloqueoDisponible(): Boolean
