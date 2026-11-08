package bas.orellana.gastostracker.presentation.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Mismos autenticadores que usa el bloqueo de `MainActivity`: biometría o el bloqueo del dispositivo. */
const val AUTENTICADORES_BLOQUEO = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

@Composable
actual fun bloqueoDisponible(): Boolean =
    BiometricManager.from(LocalContext.current).canAuthenticate(AUTENTICADORES_BLOQUEO) == BiometricManager.BIOMETRIC_SUCCESS
