package bas.orellana.apppair.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import bas.orellana.apppair.navigation.NavRoutes
import bas.orellana.apppair.presentation.ui.screen.HomeScreen
import bas.orellana.apppair.presentation.ui.screen.SplashScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(navController = navController)
        }
        composable(NavRoutes.HOME) {
            HomeScreen()
        }
    }
}