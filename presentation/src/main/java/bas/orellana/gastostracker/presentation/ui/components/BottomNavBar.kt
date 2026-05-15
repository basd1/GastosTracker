package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import bas.orellana.gastostracker.navigation.NavRoutes

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentDestination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            selected = currentRoute == NavRoutes.GREEN_SCREEN,
            onClick = {
                if (currentRoute != NavRoutes.GREEN_SCREEN) {
                    navController.navigate(NavRoutes.GREEN_SCREEN) {
                        popUpTo(NavRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            selected = currentRoute == NavRoutes.HOME,
            onClick = {
                if (currentRoute != NavRoutes.HOME) {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) },
            selected = currentRoute == NavRoutes.GRAPH_SCREEN,
            onClick = {
                if (currentRoute != NavRoutes.GRAPH_SCREEN) {
                    navController.navigate(NavRoutes.GRAPH_SCREEN) {
                        popUpTo(NavRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}