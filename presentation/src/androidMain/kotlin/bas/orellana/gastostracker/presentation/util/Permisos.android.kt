package bas.orellana.gastostracker.presentation.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

/** Android 13+ (minSdk 33): POST_NOTIFICATIONS es un permiso en tiempo de ejecución. */
@Composable
actual fun rememberPedirPermisoNotificaciones(onResultado: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val resultado = rememberUpdatedState(onResultado)
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        resultado.value(concedido)
    }
    return remember(lanzador, context) {
        {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                resultado.value(true)
            } else {
                lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
