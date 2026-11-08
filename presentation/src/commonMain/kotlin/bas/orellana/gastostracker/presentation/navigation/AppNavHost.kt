package bas.orellana.gastostracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import bas.orellana.gastostracker.presentation.TareasAlAbrir
import bas.orellana.gastostracker.presentation.ui.screen.AjustesScreen
import bas.orellana.gastostracker.presentation.ui.screen.BalanceScreen
import bas.orellana.gastostracker.presentation.ui.screen.GraphScreen
import bas.orellana.gastostracker.presentation.ui.screen.HomeScreen
import bas.orellana.gastostracker.presentation.ui.screen.PresupuestosScreen
import bas.orellana.gastostracker.presentation.ui.screen.RecurrentesScreen
import bas.orellana.gastostracker.presentation.ui.screen.CuentasScreen
import bas.orellana.gastostracker.presentation.ui.screen.MetasScreen
import bas.orellana.gastostracker.presentation.ui.screen.MetaDetalleScreen
import org.koin.compose.koinInject

/** La pantalla de bienvenida es la del sistema (SplashScreen de Android 12+), no un destino de navegación. */
@Composable
fun AppNavHost(navController: NavHostController) {
    EjecutarAlPasarAPrimerPlano()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.BALANCE) {
            BalanceScreen(navController = navController)
        }
        composable(NavRoutes.HOME) {
            HomeScreen(navController = navController)
        }
        composable(NavRoutes.GRAPH_SCREEN) {
            GraphScreen(navController = navController)
        }
        composable(NavRoutes.AJUSTES) {
            AjustesScreen(navController = navController)
        }
        composable(NavRoutes.PRESUPUESTOS) {
            PresupuestosScreen(navController = navController)
        }
        composable(NavRoutes.METAS) {
            MetasScreen(navController = navController)
        }
        composable(NavRoutes.META_DETALLE) { entrada ->
            val id = entrada.savedStateHandle.get<String>(NavRoutes.ARG_ID).orEmpty()
            MetaDetalleScreen(navController = navController, metaId = id)
        }
        composable(NavRoutes.CUENTAS) {
            CuentasScreen(navController = navController)
        }
        composable(NavRoutes.RECURRENTES) {
            RecurrentesScreen(navController = navController)
        }
    }
}

/** Lanza [TareasAlAbrir] cada vez que la app pasa a primer plano (Android e iOS). */
@Composable
private fun EjecutarAlPasarAPrimerPlano() {
    val tareas = koinInject<TareasAlAbrir>()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, tareas) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            tareas.ejecutar()
        }
    }
}
