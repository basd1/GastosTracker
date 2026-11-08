package bas.orellana.gastostracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import bas.orellana.gastostracker.presentation.ui.screen.BalanceScreen
import bas.orellana.gastostracker.presentation.ui.screen.GraphScreen
import bas.orellana.gastostracker.presentation.ui.screen.HomeScreen

/** La pantalla de bienvenida es la del sistema (SplashScreen de Android 12+), no un destino de navegación. */
@Composable
fun AppNavHost(navController: NavHostController) {
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
    }
}
