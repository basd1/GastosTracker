package bas.orellana.gastostracker.iosapp

import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import bas.orellana.gastostracker.di.iniciarKoin
import bas.orellana.gastostracker.presentation.navigation.AppNavHost
import platform.UIKit.UIViewController

// MainViewController() lo llama Swift una sola vez al arrancar la app, siempre
// desde el hilo principal, así que no hace falta sincronización adicional aquí.
private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        // Incluye los ViewModels: antes solo se registraban en :app (Android) y en iOS faltaban.
        iniciarKoin()
        koinStarted = true
    }
    return ComposeUIViewController {
        AppNavHost(navController = rememberNavController())
    }
}
