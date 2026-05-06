package bas.orellana.gastostracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import bas.orellana.gastostracker.navigation.NavRoutes
import bas.orellana.gastostracker.presentation.ui.screen.BlueScreen
import bas.orellana.gastostracker.presentation.ui.screen.GreenScreen
import bas.orellana.gastostracker.presentation.ui.screen.HomeScreen
import bas.orellana.gastostracker.presentation.ui.screen.SplashScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(navController = navController)
        }
        composable(NavRoutes.GREEN_SCREEN) {
            GreenScreen(navController = navController)
        }
        composable(NavRoutes.HOME) {
            HomeScreen(navController = navController)
        }
        composable(NavRoutes.BLUE_SCREEN) {
            BlueScreen(navController = navController)
        }
    }
}