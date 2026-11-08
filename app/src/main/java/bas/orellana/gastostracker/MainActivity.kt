package bas.orellana.gastostracker

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import bas.orellana.gastostracker.presentation.navigation.AppNavHost
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import bas.orellana.gastostracker.ui.theme.GastosTrackerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mantenerSplashHastaCargarPreferencias()
        setContent {
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

            GastosTrackerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                }
            }
        }
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
}
