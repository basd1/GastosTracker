package bas.orellana.gastostracker

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import bas.orellana.gastostracker.presentation.navigation.AccionesPendientes
import bas.orellana.gastostracker.presentation.navigation.AppNavHost
import bas.orellana.gastostracker.presentation.ui.screen.PantallaBloqueo
import bas.orellana.gastostracker.presentation.util.AUTENTICADORES_BLOQUEO
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import bas.orellana.gastostracker.ui.theme.GastosTrackerTheme
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

/** FragmentActivity (y no ComponentActivity) porque BiometricPrompt lo necesita. */
class MainActivity : FragmentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModel()

    /** Hay que desbloquear (si el bloqueo está activo): al arrancar y al volver tras un rato fuera. */
    private var necesitaDesbloqueo by mutableStateOf(true)

    /** Momento en que la app pasó a segundo plano (para no bloquear si solo se fue unos segundos). */
    private var salidaEn: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mantenerSplashHastaCargarPreferencias()
        atenderIntent(intent)
        setContent {
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            val ajustes by settingsViewModel.ajustes.collectAsState()
            val cargadas by settingsViewModel.preferenciasCargadas.collectAsState()
            val bloqueada = cargadas && ajustes.bloqueoActivo && necesitaDesbloqueo

            // Sin bloqueo no hay nada que desbloquear (y activarlo no bloquea en el acto).
            LaunchedEffect(cargadas, ajustes.bloqueoActivo) {
                if (cargadas && !ajustes.bloqueoActivo) necesitaDesbloqueo = false
            }
            LaunchedEffect(bloqueada) {
                if (bloqueada) pedirDesbloqueo()
            }

            GastosTrackerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        AppNavHost(navController = navController)
                        if (bloqueada) PantallaBloqueo(onDesbloquear = ::pedirDesbloqueo)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val salida = salidaEn
        if (salida != null && SystemClock.elapsedRealtime() - salida > MARGEN_BLOQUEO_MS) necesitaDesbloqueo = true
        salidaEn = null
    }

    override fun onStop() {
        super.onStop()
        // Abrir un selector de archivos o de permisos también para la actividad: por eso hay un margen.
        salidaEn = SystemClock.elapsedRealtime()
    }

    private fun pedirDesbloqueo() {
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    necesitaDesbloqueo = false
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setAllowedAuthenticators(AUTENTICADORES_BLOQUEO)
            .build()
        prompt.authenticate(info)
    }

    /**
     * La pantalla de bienvenida es la del sistema (Android 12+, ver `windowSplashScreen*` en el tema).
     * Se mantiene hasta leer el tema guardado: así la app no se pinta en claro y salta a oscuro.
     */
    private fun mantenerSplashHastaCargarPreferencias() {
        val contenido: View = findViewById(android.R.id.content)
        contenido.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (!settingsViewModel.preferenciasCargadas.value) return false
                contenido.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        atenderIntent(intent)
    }

    /** Atajo del icono, widget o notificación: abrir directamente el diálogo de añadir gasto. */
    private fun atenderIntent(intent: Intent?) {
        if (intent?.action == ACCION_ANADIR_GASTO) {
            get<AccionesPendientes>().pedirAnadirGasto()
            // Para no repetirlo al recrear la actividad (p. ej. al girar la pantalla).
            intent.action = null
        }
    }

    companion object {
        const val ACCION_ANADIR_GASTO = "bas.orellana.gastostracker.ANADIR_GASTO"

        /** Si la app vuelve a primer plano antes de este tiempo, no se vuelve a pedir el desbloqueo. */
        private const val MARGEN_BLOQUEO_MS = 30_000L
    }
}
