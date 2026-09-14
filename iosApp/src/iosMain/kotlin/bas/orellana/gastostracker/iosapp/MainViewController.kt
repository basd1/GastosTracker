package bas.orellana.gastostracker.iosapp

import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import bas.orellana.gastostracker.di.platformModule
import bas.orellana.gastostracker.di.sharedModule
import bas.orellana.gastostracker.presentation.navigation.AppNavHost
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

// MainViewController() lo llama Swift una sola vez al arrancar la app, siempre
// desde el hilo principal, así que no hace falta sincronización adicional aquí.
private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        startKoin {
            modules(sharedModule, platformModule)
        }
        koinStarted = true
    }
    return ComposeUIViewController {
        AppNavHost(navController = rememberNavController())
    }
}
