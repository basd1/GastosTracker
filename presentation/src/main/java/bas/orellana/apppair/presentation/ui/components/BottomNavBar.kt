package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import bas.orellana.gastostracker.navigation.NavRoutes

val bottomNavItems = listOf(
    NavRoutes.GREEN_SCREEN,
    NavRoutes.HOME,
    NavRoutes.BLUE_SCREEN
)

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentDestination?.route

    NavigationBar {
        bottomNavItems.forEachIndexed { index, route ->
            NavigationBarItem(
                icon = { },
                label = { },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}